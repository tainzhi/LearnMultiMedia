# LearnMultiMedia
## Basic
### 三种绘图方法
- 从asset目录导入图片流到bitmap绘制到ImageView，SurfaceView， 自定义View。
- canvas.drawBitmap使用Rect和scale matrix铺满
- `SurfaceHolder.Callback`的`onSurfaceCreated`之后会调用`onSurfaceChanged`
- [参考:Android 音视频开发(一) : 通过三种方式绘制图片](https://www.cnblogs.com/renhui/p/7456956.html)

### AudioRecod录制音频文件，AudioTrack播放音频文件
- AudioRecod录制音频PCM文件，比MediaRecord更底层
- PCM文件需要添加wav文件头，才能被AudioTrack播放
- AudioTrack有stream和staic两种播放方式
- [参考：使用 AudioRecord 采集音频PCM并保存到文件](https://www.cnblogs.com/renhui/p/7457321.html
), [参考：使用 AudioTrack 播放PCM音频](https
://www.cnblogs.com/renhui/p/7463287.html)

