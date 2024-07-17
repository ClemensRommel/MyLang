package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

import std.ffi._array;

public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var198) {
        return std.fs.open_file(__var198);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var199 = isNull(unsafe_file);
        if(__var199) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var200) {
        return java.nio.file.Files.exists(__var200);
    }
    public static String unsafe_read_file(java.nio.file.Path __var201) {
        return std.fs.read_file(__var201);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var202 = isNull(unsafe_content);
        if(__var202) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var203, String __var204) {
        return std.fs.write_to_file(__var203, __var204);
    }
    public static String unsafe_write_binary(java.nio.file.Path __var205, byte[] __var206) {
        return std.fs.write_bytearray(__var205, __var206);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
        var __var207 = isNull(err_msg);
        final boolean __var208;
        if(!__var207) {
            __var208 = !__equal(err_msg, "");
            
        } else {
            __var208 =  true;
        }
        if(__var208) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void write_bytes(java.nio.file.Path file, byte[] bytes) {
        final String err_msg;
        err_msg = unsafe_write_binary(file, bytes);
        var __var209 = isNull(err_msg);
        final boolean __var210;
        if(!__var209) {
            __var210 = !__equal(err_msg, "");
            
        } else {
            __var210 =  true;
        }
        if(__var210) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        write(path, "");
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var211) {
        std.fs.delete(__var211);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var212 = create_file_path(path);
        mylmyl.__generated_Main.__var62<java.nio.file.Path, std._implicit.Optional<String>> __var213 = (java.nio.file.Path __var214) -> read_string(__var214);
        return __var212.flatmap(__var213);
        
    }
    public static <T>boolean isNull(T __var215) {
        return std.BuiltinFunctions.isNull(__var215);
    }
    
}
