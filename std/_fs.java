package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var190) {
        return std.fs.open_file(__var190);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var191 = isNull(unsafe_file);
        if(__var191) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var192) {
        return java.nio.file.Files.exists(__var192);
    }
    public static String unsafe_read_file(java.nio.file.Path __var193) {
        return std.fs.read_file(__var193);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var194 = isNull(unsafe_content);
        if(__var194) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var195, String __var196) {
        return std.fs.write_to_file(__var195, __var196);
    }
    public static String unsafe_write_binary(java.nio.file.Path __var197, byte[] __var198) {
        return std.fs.write_bytearray(__var197, __var198);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
        var __var199 = isNull(err_msg);
        final boolean __var200;
        if(!__var199) {
            __var200 = !__equal(err_msg, "");
            
        } else {
            __var200 =  true;
        }
        if(__var200) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void write_bytes(java.nio.file.Path file, byte[] bytes) {
        final String err_msg;
        err_msg = unsafe_write_binary(file, bytes);
        var __var201 = isNull(err_msg);
        final boolean __var202;
        if(!__var201) {
            __var202 = !__equal(err_msg, "");
            
        } else {
            __var202 =  true;
        }
        if(__var202) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        write(path, "");
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var203) {
        std.fs.delete(__var203);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var204 = create_file_path(path);
        mylmyl.__generated_Main.__var63<java.nio.file.Path, std._implicit.Optional<String>> __var205 = (java.nio.file.Path __var206) -> read_string(__var206);
        return __var204.flatmap(__var205);
        
    }
    public static <T>boolean isNull(T __var207) {
        return std.BuiltinFunctions.isNull(__var207);
    }
    
}
