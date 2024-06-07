package std;

import static std.runtime.Functions.*;
import std.runtime.*;

import java.util.ArrayList;


public class _fs {
    public static java.nio.file.Path unsafe_create_file_path(String __var327) {
        return std.fs.open_file(__var327);
    }
    public static std._implicit.Optional<java.nio.file.Path> create_file_path(String path) {
        final java.nio.file.Path unsafe_file;
        unsafe_file = unsafe_create_file_path(path);
        var __var328 = isNull(unsafe_file);
        if(__var328) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_file);
            
        }
        
    }
    
    public static boolean file_exists(java.nio.file.Path __var329) {
        return java.nio.file.Files.exists(__var329);
    }
    public static String unsafe_read_file(java.nio.file.Path __var330) {
        return std.fs.read_file(__var330);
    }
    public static std._implicit.Optional<String> read_string(java.nio.file.Path file) {
        final String unsafe_content;
        unsafe_content = unsafe_read_file(file);
        var __var331 = isNull(unsafe_content);
        if(__var331) {
            return new std._implicit.None();
            
        } else {
            return new std._implicit.Some(unsafe_content);
            
        }
        
    }
    
    public static String unsafe_write_file(java.nio.file.Path __var332, String __var333) {
        return std.fs.write_to_file(__var332, __var333);
    }
    public static Void write(java.nio.file.Path file, String new_content) {
        final String err_msg;
        err_msg = unsafe_write_file(file, new_content);
        var __var334 = isNull(err_msg);
        final boolean __var335;
        if(!__var334) {
            __var335 = !__equal(err_msg, "");
            
        } else {
            __var335 =  true;
        }
        if(__var335) {
            {
                __ignore(panic(err_msg));
                
            }
            
        }return null;
    }
    
    public static Void create_file(java.nio.file.Path path) {
        __ignore(write(path, ""));
        return null;
    }
    
    public static Void delete(java.nio.file.Path __var336) {
        std.fs.delete(__var336);
        return null;
    }
    public static std._implicit.Optional<String> open_and_read(String path)  {
        var __var337 = create_file_path(path);
        mylmyl.__generated_Main.__var62<java.nio.file.Path, std._implicit.Optional<String>> __var338 = (java.nio.file.Path __var339) -> read_string(__var339);
        return __var337.flatmap(__var338);
        
    }
    public static <T>boolean isNull(T __var340) {
        return std.BuiltinFunctions.isNull(__var340);
    }
    
}
