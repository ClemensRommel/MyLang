package MyLang;

import java.util.*;
import java.util.stream.Collectors;

import static MyLang.MyLangAST.*;

public class TypeEnv implements TypeRepVisitor<TypeRep> {
    // Typechecker der normalisierung will
    private Typechecker tc = null;
    private Map<String, TypeRep> values = new HashMap<>();
    private Set<String> exportedValues = new HashSet<>();
    private Map<String, TypeRep> types = new HashMap<>();
    private Set<String> exportedTypes = new HashSet<>();
    private Map<String, Boolean> reassignability = new HashMap<>();

    private TypeEnv outer = null;

    public TypeEnv openScope() {
        var newEnv = new TypeEnv();
        newEnv.outer = this;
        return newEnv;
    }

    public TypeEnv closeScope() {
        return outer;
    }

    public TypeRep getTypeOfValue(String name, Typechecker t) {
        this.tc = t;
        if(values.containsKey(name)) {
            return values.get(name);
        } else if(outer != null) {
            return outer.getTypeOfValue(name, tc);
        } else {
            tc.error("Tried to get type of non-existent value '"+name+"'");
            return Typechecker.unknown();
        }
    }

    public void declareModule(MyLangAST.Module module, MyLangPath path) {
        var currentValues = values;
        var currentTypes = types;
        Set<String> currentExportedValues = new HashSet<>();
        Set<String> currentExportedTypes = new HashSet<>();
        for(int i = 0; i < path.names().size()-1; i++) {
            var name = path.names().get(i);
            if(currentValues.containsKey(name.lexeme())) {
                currentValues = ((MyLangAST.Module) currentValues.get(name.lexeme())).enviroment().values;
                currentTypes = ((MyLangAST.Module) currentTypes.get(name.lexeme())).enviroment().types;
            } else {
                var newModule = new MyLangAST.Module(name.lexeme(), new TypeEnv());
                currentValues.put(name.lexeme(), newModule);
                currentTypes.put(name.lexeme(), newModule);
                currentExportedTypes.add(name.lexeme());
                currentExportedValues.add(name.lexeme());
                currentValues = newModule.enviroment().values;
                currentTypes = newModule.enviroment().types;
                currentExportedValues = newModule.enviroment().exportedValues;
                currentExportedTypes = newModule.enviroment().exportedTypes;
            }
        }
        var finalName = path.names().get(path.names().size()-1).lexeme();
        currentValues.put(finalName, module);
        currentTypes.put(finalName, module);
        currentExportedValues.add(finalName);
        currentExportedTypes.add(finalName);
    }
    public boolean valueExists(String name) {
        return values.containsKey(name) || (outer != null && outer.valueExists(name));
    }
    public boolean typeExists(String name) {
        return types.containsKey(name) || (outer != null && outer.typeExists(name));
    }

    public void exportValue(String name) {
        exportedValues.add(name);
    }

    public boolean valueExported(String name) {
        return exportedValues.contains(name);
    }

    public void exportType(String name) {
        exportedTypes.add(name);
    }
    public boolean typeExported(String name) {
        return exportedTypes.contains(name);
    }

    public TypeRep getTypeByName(String name, Typechecker t) {
        if(types.containsKey(name)) {
            return types.get(name);
        } else if(outer != null) {
            return outer.getTypeByName(name, t);
        } else {
            t.error("Tried to get unknown type" + name);
            return Typechecker.unknown();
        }
    }

    public void addType(String name, TypeRep type) {
        types.put(name, type);
    }
    public void addValue(String name, TypeRep type, boolean isReassigneable) {
        values.put(name, type);
        reassignability.put(name, isReassigneable);
    }

    public Map<String, TypeRep> getDeclaredTypesOfValues() {
        return values;
    }
    public boolean isReassigneable(String name) {
        if(reassignability.containsKey(name)) {
            return reassignability.get(name);
        }
        if(outer != null) {
            return outer.isReassigneable(name);
        }
        return false;
    }

    public TypeRep normalize(TypeRep t, Typechecker tc) {
        this.tc = tc;
        if(t == null) {
            return null;
        }
        return t.accept(this);
    }

    @Override
    public TypeRep visitBuiltin(Builtin b) {
        return b;
    }
    @Override
    public TypeRep visitListOfRep(ListOfRep l) {
        return new ListOfRep(normalize(l.elements(), tc));
    }
    @Override
    public TypeRep visitUnknownType(UnknownType t) {
        return t;
    }
    @Override
    public TypeRep visitFunctionTypeRep(FunctionTypeRep f) {
        return new FunctionTypeRep(f.parameters().stream().map(t -> normalize(t, tc)).toList(),
                                   f.optionalParameters().stream().map(t -> normalize(t, tc)).toList(),
                                   normalizeNamed(f.named()),
                                   normalizeNamed(f.optionalNamed()),
                                   normalize(f.varargsType(), tc),
                                   normalize(f.returnType(), tc),
                                   f.env());
    }

    private Map<String, TypeRep> normalizeNamed(Map<String, TypeRep> named) {
        return named.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                value -> normalize(value.getValue(), tc)
                )
            );
    }

    @Override
    public TypeRep visitClassType(ClassType c) {
        return c;
    }
    @Override
    public TypeRep visitModule(MyLangAST.Module m) {
        return m;
    }
    @Override
    public TypeRep visitTypeIdentifierRep(TypeIdentifierRep ti) {
        var fromEnv = ti.env();
        if(!fromEnv.typeExists(ti.name().lexeme())) {
            tc.error("["+ti.name().line()+"] Unknown Type '"+ti.name().lexeme()+"'");
        }
        return fromEnv.getTypeByName(ti.name().lexeme(), tc);
    }
    @Override
    public TypeRep visitAccessRep(AccessRep a) {
        var from = normalize(a.accessed(), tc);
        if(from instanceof MyLangAST.Module m) {
            /*if(!m.enviroment().typeExported(a.name().lexeme())) {
                tc.error("Module does not export type '"+a.name().lexeme()+"'");
                return Typechecker.unknown();
            }*/
            if(!m.enviroment().typeExists(a.name().lexeme())) {
                tc.error("Tried to access non-existent type '"+a.name().lexeme()+"'");
            }
            return normalize(m.enviroment().getTypeByName(a.name().lexeme(), tc), tc);
        } else {
            tc.error("Cannot access type from non-Module-Type '"+from+"'");
            return Typechecker.unknown();
        }
    }
    @Override
    public TypeRep visitEnumType(EnumType e) {
        return e;
    }

    @Override
    public String toString() {
        return "";
    }
    public String asString() {
        return "("+types.toString()+values.toString()+")\n"+(outer != null ? outer.asString() : "");
    }
    @Override
    public TypeRep visitNever(Never n) {
        return n;
    }
    @Override
    public TypeRep visitTupleRep(TupleRep t) {
        return new TupleRep(t.elements().stream().map(e -> normalize(e, tc)).toList());
    }
    @Override
    public TypeRep visitTypeVar(TypeVar v) {
        return v;
    }
    @Override
    public TypeRep visitGenericType(GenericType g) {
        return new GenericType((TypeFunction) g.t().accept(this));
    }
    @Override
    public TypeRep visitTypeFunction(TypeFunction t) {
        return new TypeFunction(t.typeParams(), normalize(t.body(), tc), t.env());
    }
    @Override
    public TypeRep visitTypeApplication(TypeApplication t) {
        TypeFunction tf;
        var callee = normalize(t.applied(), tc);
        if(callee instanceof TypeFunction tf2) {
            tf = tf2;
        } else {
            tc.error("Tried to apply non-type-function "+tc.p.prettyPrint(callee)+"of type"+ callee.getClass()+" to type parameters "+t.params()
                .stream().map(tc.p::prettyPrint).toList());
            return Typechecker.unknown();
        }
        return normalize(
            tc.ta.apply(
                tf,
                t.params().stream().map(tp -> normalize(tp, tc)).toList(),
                false),
            tc);
    }
}
