package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var326) {
        return std.fs.open_file(__var326);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var327 = isFileNull(unsafe_file);
        if(__var327) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var328) {
        return java.nio.file.Files.exists(__var328);
    }
    public static String unsafe_read_file(java.nio.file.Path __var329) {
        return std.fs.read_file(__var329);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var330 = isStringNull(unsafe_content);
        if(__var330) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var331, String __var332) {
        return std.fs.write_to_file(__var331, __var332);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
        var __var333 = isStringNull(err_msg);
        final boolean __var334;
        if(!__var333) {
            __var334 = !__equal(err_msg, "");
            
        } else {
            __var334 =  true;
        }
        if(__var334) {
            {
                __ignore(panic(err_msg));
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        __ignore(write(path, ""));
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var335) {
        std.fs.delete(__var335);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var336 = create_file_path(path);
        switch(__var336) {
            case std._implicit.Some<java.nio.file.Path>(java.nio.file.Path file) -> {
                return read_string(file);
                
            }
            case std._implicit.Optional<java.nio.file.Path> __var337 -> {
                return new std._implicit.None();
                
            }
            
        }
        
    }
    public static boolean isFileNull(java.nio.file.Path __var338) {
        return std.BuiltinFunctions.isNull(__var338);
    }
    public static boolean isStringNull(String __var339) {
        return std.BuiltinFunctions.isNull(__var339);
    }
    
}
