package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;

import std.ffi._array;

public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var210) {
        return std.fs.open_file(__var210);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var211 = isNull(unsafe_file);
        if(__var211) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var212) {
        return java.nio.file.Files.exists(__var212);
    }
    public static String unsafe_read_file(java.nio.file.Path __var213) {
        return std.fs.read_file(__var213);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var214 = isNull(unsafe_content);
        if(__var214) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var215, String __var216) {
        return std.fs.write_to_file(__var215, __var216);
    }
    public static String unsafe_write_binary(java.nio.file.Path __var217, byte[] __var218) {
        return std.fs.write_bytearray(__var217, __var218);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
        var __var219 = isNull(err_msg);
        final boolean __var220;
        if(!__var219) {
            __var220 = !__equal(err_msg, "");
            
        } else {
            __var220 =  true;
        }
        if(__var220) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void write_bytes(java.nio.file.Path file, byte[] bytes) {
        final String err_msg;
        err_msg = unsafe_write_binary(file, bytes);
        var __var221 = isNull(err_msg);
        final boolean __var222;
        if(!__var221) {
            __var222 = !__equal(err_msg, "");
            
        } else {
            __var222 =  true;
        }
        if(__var222) {
            {
                panic(err_msg);
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        write(path, "");
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var223) {
        std.fs.delete(__var223);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var224 = create_file_path(path);
        mylmyl.__generated_Main.__var62<java.nio.file.Path, std._implicit.Optional<String>> __var225 = (java.nio.file.Path __var226) -> read_string(__var226);
        return __var224.flatmap(__var225);
        
    }
    public static <T>boolean isNull(T __var227) {
        return std.BuiltinFunctions.isNull(__var227);
    }
    
}
