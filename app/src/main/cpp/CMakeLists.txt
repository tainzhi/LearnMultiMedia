cmake_minimum_required(VERSION 3.18.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -UNDEBUG")

add_library( # Specifies the name of the library.
        native-codec-jni
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).

        looper.cpp
        looper.h
        native-codec-jni.cpp )

# Include libraries needed for native-codec-jni lib
target_link_libraries(native-codec-jni
        android
        log
        mediandk
        OpenMAXAL)

