#include <jni.h>
#include <android/log.h>
#include "opencv2/opencv.hpp"
#include "opencv2/core/ocl.hpp"
#include "opencv2/core.hpp"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, __VA_ARGS__)

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_CamApp_initOpenCV(JNIEnv *env, jobject thiz) {
    cv::Mat mat;
    LOGV("qfq", "opcv init");
//    https://github.com/opencv/opencv/wiki/OpenCL-optimizations
    cv::ocl::Context ctx = cv::ocl::Context::getDefault();
    if (!ctx.ptr())
    {
        LOGV("qfq", "opencv:opencl is not available");
    } else {
        LOGV("qfq", "opencv:opencl is available");
    }
    cv::UMat umat;
}