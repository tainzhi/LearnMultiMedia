cmake_minimum_required(VERSION 3.22.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17 -Wall -UNDEBUG")

project("LearnMultiMedia")

include("${CMAKE_SOURCE_DIR}/opencv_4.8/sdk/native/jni/OpenCVConfig.cmake")
add_library( # Specifies the name of the library.
        opencv
        # Sets the library as a shared library.
        SHARED
        opencv-jni.cpp
        image-processor.cpp
        )
include_directories(${OpenCV_INCLUDE_DIRS}) # Not needed for CMake >= 2.8.11
message("opencv_include_dirs=${OpenCV_INCLUDE_DIRS}")
target_link_libraries(opencv
        log
        ${OpenCV_LIBS})

add_library( # Specifies the name of the library.
        native-codec-jni
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).

        looper.cpp
        looper.h
        native-codec-jni.cpp )
# Include libraries needed for native-codec-jni lib
target_link_libraries(
        native-codec-jni
        android
        log
        mediandk
        OpenMAXAL)

