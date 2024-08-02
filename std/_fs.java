package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

import std.ffi._array;

public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var191) {
        return std.fs.open_file(__var191);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var192 = isNull(unsafe_file);
        if(__var192) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var193) {
        return java.nio.file.Files.exists(__var193);
    }
    public static String unsafe_read_file(java.nio.file.Path __var194) {
        return std.fs.read_file(__var194);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var195 = isNull(unsafe_content);
        if(__var195) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var196, String __var197) {
        return std.fs.write_to_file(__var196, __var197);
    }
    public static String unsafe_write_binary(java.nio.file.Path __var198, byte[] __var199) {
        return std.fs.write_bytearray(__var198, __var199);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
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
    
    public static Void write_bytes(java.nio.file.Path file, byte[] bytes) {
        final String err_msg;
        err_msg = unsafe_write_binary(file, bytes);
        var __var202 = isNull(err_msg);
        final boolean __var203;
        if(!__var202) {
            __var203 = !__equal(err_msg, "");
            
        } else {
            __var203 =  true;
        }
        if(__var203) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        write(path, "");
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var204) {
        std.fs.delete(__var204);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var205 = create_file_path(path);
        mylmyl.__generated_Main.__var64<java.nio.file.Path, std._implicit.Optional<String>> __var206 = (java.nio.file.Path __var207) -> read_string(__var207);
        return __var205.flatmap(__var206);
        
    }
    public static <T>boolean isNull(T __var208) {
        return std.BuiltinFunctions.isNull(__var208);
    }
    
}
