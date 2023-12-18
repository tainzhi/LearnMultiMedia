#include <jni.h>
#include <android/log.h>
#include "opencv2/opencv.hpp"
#include "opencv2/core/ocl.hpp"
#include "opencv2/core.hpp"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, __VA_ARGS__)
#define TAG "NativeOpenCV"

extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_camera_ImageProcessor_init(JNIEnv *env, jobject thiz) {
    cv::Mat mat;
    cv::UMat umat;
    LOGV(TAG, "opencv init");
    // https://github.com/opencv/opencv/wiki/OpenCL-optimizations
    cv::ocl::Context ctx = cv::ocl::Context::getDefault();
    if (!ctx.ptr())
    {
        LOGV(TAG, "opencv:opencl is not available");
    } else {
        LOGV(TAG, "opencv:opencl is available");
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_camera_ImageProcessor_deinit(JNIEnv *env, jobject thiz) {
    LOGV(TAG, "opencv deinit");
}
//extern "C"
//JNIEXPORT void JNICALL
//Java_com_tainzhi_sample_media_camera_ImageProcessor_processImage(JNIEnv *env, jobject thiz,
//                                                                 jbyteArray y_plane,
//                                                                 jbyteArray u_plane,
//                                                                 jbyteArray v_plane, jint width,
//                                                                 jint height) {
//    cv::Mat yuvMat(height, width, CV_8UC3);
//    LOGD(TAG, "processImage, width:%d height:%d", width, height);
//    // Get the Y plane
//    jboolean isCopy;
//    jbyte* yPlane = env->GetByteArrayElements(y_plane, &isCopy);
//    for (int i = 0; i < height; i++) {
//        for (int j = 0; j < width; j++) {
//            yuvMat.at<uchar>(i, j) = yPlane[i * width + j];
//        }
//    }
//    env->ReleaseByteArrayElements(y_plane,  yPlane, JNI_ABORT);
//
//    // Get the U plane
//    jbyte* uPlane = env->GetByteArrayElements(u_plane, &isCopy);
//    for (int i = 0; i < height / 2; i++) {
//        for (int j = 0; j < width / 2; j++) {
//            yuvMat.at<uchar>(i * 2, j * 2) = uPlane[i * width / 2 + j];
//        }
//    }
//    env->ReleaseByteArrayElements(u_plane, uPlane, JNI_ABORT);
//
//    // Get the V plane
//    jbyte* vPlane = env->GetByteArrayElements(v_plane, &isCopy);
//    for (int i = 0; i < height / 2; i++) {
//        for (int j = 0; j < width / 2; j++) {
//            yuvMat.at<uchar>(i * 2 + 1, j * 2) = vPlane[i * width / 2 + j];
//        }
//    }
//    env->ReleaseByteArrayElements(v_plane, vPlane, JNI_ABORT);
//
//    // Convert YUV to BGR
//    cv::Mat bgrMat;
//    cv::cvtColor(yuvMat, bgrMat, cv::COLOR_YUV2BGR_I420);
//
////    // Copy the bitmap pixels
////    env->GetDirectBufferAddress(bitmapOutput);
////    uint8_t* bitmapPixels = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(bitmapOutput));
////    for (int i = 0; i < height; i++) {
////        for (int j = 0; j < width; j++) {
////            int bitmapIndex = (i * rowBytes + j * 3);
////            int colorIndex = i * width * 3 + j * 3;
////            bitmapPixels[bitmapIndex] = bgrMat.at<Vec3b>(i, j)[2];
////            bitmapPixels[bitmapIndex + 1] = bgrMat.at<Vec3b>(i, j)[1];
////            bitmapPixels[bitmapIndex + 2] = bgrMat.at<Vec3b>(i, j)[0];
////        }
////    }
//}
extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_camera_ImageProcessor_processImage(JNIEnv *env, jobject thiz,
                                                                 jobject y_plane, jobject u_plane,
                                                                 jobject v_plane, jint width,
                                                                 jint height) {
    cv::Mat yuvMat(height, width, CV_8UC3);
    LOGD(TAG, "processImage, width:%d height:%d", width, height);
    // Get the Y plane
    jboolean isCopy;
    jbyte* yPlane = (jbyte*) env->GetDirectBufferAddress(y_plane);
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            yuvMat.at<uchar>(i, j) = yPlane[i * width + j];
        }
    }

    // Get the U plane
    jbyte* uPlane = (jbyte*)env->GetDirectBufferAddress(u_plane);
    for (int i = 0; i < height / 2; i++) {
        for (int j = 0; j < width / 2; j++) {
            yuvMat.at<uchar>(i * 2, j * 2) = uPlane[i * width / 2 + j];
        }
    }

    // Get the V plane
    jbyte* vPlane = (jbyte*)env->GetDirectBufferAddress(v_plane);
    for (int i = 0; i < height / 2; i++) {
        for (int j = 0; j < width / 2; j++) {
            yuvMat.at<uchar>(i * 2 + 1, j * 2) = vPlane[i * width / 2 + j];
        }
    }

    // Convert YUV to BGR
    cv::Mat bgrMat;
    cv::cvtColor(yuvMat, bgrMat, cv::COLOR_YUV2BGR_I420);

//    // Copy the bitmap pixels
//    env->GetDirectBufferAddress(bitmapOutput);
//    uint8_t* bitmapPixels = reinterpret_cast<uint8_t*>(env->GetDirectBufferAddress(bitmapOutput));
//    for (int i = 0; i < height; i++) {
//        for (int j = 0; j < width; j++) {
//            int bitmapIndex = (i * rowBytes + j * 3);
//            int colorIndex = i * width * 3 + j * 3;
//            bitmapPixels[bitmapIndex] = bgrMat.at<Vec3b>(i, j)[2];
//            bitmapPixels[bitmapIndex + 1] = bgrMat.at<Vec3b>(i, j)[1];
//            bitmapPixels[bitmapIndex + 2] = bgrMat.at<Vec3b>(i, j)[0];
//        }
//    }

}