package MyLang;

import java.util.List;

public record EnumVariantObject(List<Object> fields, EnumVariant variant) {
    @Override
    public String toString() {
        return variant.Name().lexeme()+fields.toString();
    }
    public Object getProperty(String name) {
        return variant().methods().get(name).bind(this);
    }
}
