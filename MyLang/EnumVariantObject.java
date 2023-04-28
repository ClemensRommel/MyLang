package MyLang;

import java.util.List;

public record EnumVariantObject(List<Object> fields, EnumVariant variant) {
    @Override
    public String toString() {
        return variant.Name().lexeme()+fields.toString();
    }
}
