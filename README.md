# LearnMultiMedia
## Basic
### 三种绘图方法
- 从asset目录导入图片流到bitmap绘制到ImageView，SurfaceView， 自定义View。
- canvas.drawBitmap使用Rect和scale matrix铺满
- `SurfaceHolder.Callback`的`onSurfaceCreated`之后会调用`onSurfaceChanged`
- [参考:Android 音视频开发(一) : 通过三种方式绘制图片](https://www.cnblogs.com/renhui/p/7456956.html)