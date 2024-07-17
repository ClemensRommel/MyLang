package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

import std.ffi._array;

public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var189) {
        return std.fs.open_file(__var189);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var190 = isNull(unsafe_file);
        if(__var190) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var191) {
        return java.nio.file.Files.exists(__var191);
    }
    public static String unsafe_read_file(java.nio.file.Path __var192) {
        return std.fs.read_file(__var192);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var193 = isNull(unsafe_content);
        if(__var193) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var194, String __var195) {
        return std.fs.write_to_file(__var194, __var195);
    }
    public static String unsafe_write_binary(java.nio.file.Path __var196, byte[] __var197) {
        return std.fs.write_bytearray(__var196, __var197);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
        var __var198 = isNull(err_msg);
        final boolean __var199;
        if(!__var198) {
            __var199 = !__equal(err_msg, "");
            
        } else {
            __var199 =  true;
        }
        if(__var199) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void write_bytes(java.nio.file.Path file, byte[] bytes) {
        final String err_msg;
        err_msg = unsafe_write_binary(file, bytes);
        var __var200 = isNull(err_msg);
        final boolean __var201;
        if(!__var200) {
            __var201 = !__equal(err_msg, "");
            
        } else {
            __var201 =  true;
        }
        if(__var201) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        write(path, "");
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var202) {
        std.fs.delete(__var202);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var203 = create_file_path(path);
        mylmyl.__generated_Main.__var62<java.nio.file.Path, std._implicit.Optional<String>> __var204 = (java.nio.file.Path __var205) -> read_string(__var205);
        return __var203.flatmap(__var204);
        
    }
    public static <T>boolean isNull(T __var206) {
        return std.BuiltinFunctions.isNull(__var206);
    }
    
}
