# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("lame")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0 -g -DSTDC_HEADERS -DHAVE_MPGLIB")
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

#指定头文件所在，可以多次调用，指定多个路径
include_directories(src/main/cpp/lame)

add_library( # Sets the name of the library.
        lame

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        lame.cpp
        LameControl.cpp
        LameDecode.cpp
        lame/bitstream.c
        lame/encoder.c
        lame/fft.c
        lame/gain_analysis.c
        lame/id3tag.c
        lame/lame.c
        lame/mpglib_interface.c
        lame/newmdct.c
        lame/presets.c
        lame/psymodel.c
        lame/quantize.c
        lame/quantize_pvt.c
        lame/reservoir.c
        lame/set_get.c
        lame/tables.c
        lame/takehiro.c
        lame/util.c
        lame/vbrquantize.c
        lame/VbrTag.c
        lame/version.c
        lame/common.c
        lame/dct64_i386.c
        lame/decode_i386.c
        lame/interface.c
        lame/layer1.c
        lame/layer2.c
        lame/layer3.c
        lame/tabinit.c
        )
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        lame

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})