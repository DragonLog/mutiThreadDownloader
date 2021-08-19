> **写在前面的话：项目copy下来后，直接用idea导入应该是没有问题的（项目的文件夹和包结构要正确），之后到Main类下执行main方法就可以运行程序了！**
## 多线程下载器（**原生JDK**）
***
> **使用原生 JDK进行开发**
1. **该项目体量不大，没有使用JDK以外的任何框架，旨在深入理解和应用java多线程；**        
2. **使用了自定义日志类，配合newScheduledThreadPool线程池能够周期性打印下载信息日志；** 
3. **使用了LongAdder原子类，保证了该类实例变量原子性和可见性；**
4. **输入输出流的使用以及RandomAccessFile类的使用实现了灵活的读写，拆分和组合文件；**   
5. **HttpURLConnection类网络请求实现分段（range）下载文件；**   
6. **ThreadPoolExecutor线程池实现了多线程任务的下载；**
7. **CountDownLatch类等待线程任务完成后调度；**
8. **原项目的初级版本是没有“断点续传”功能的，但老师说可以根据finishedSize这个原子实例变量把该项目进行扩展从而增加断点续传功能。在深入理解代码逻辑后会发现这其实不难，大概的逻辑就是使用文件类去求已下载文件的总下载量，如果大于0说明要进行断点续传，之后在split函数里，把分配给每个线程下载任务的头和尾重新计算即可，其中要注意RandomAccessFile和原子实例变量的使用。另外老师还说，这种分发任务下去，最后回收任务的“总分总”形式也很适合用ForkJoinPool线程池进行优化，但由于我没怎么实际使用过这个线程池，而且以我的经验来看使用ForkJoinPool线程池的话项目总体结构可能会大改，能力有限的我只好把这个问题留给有兴趣的同学继续完善啦！**
> **注：此程序为控制台程序，有兴趣的同学可以为其添加UI界面使其更像是一款真正的下载器。** 
***
> **[动力节点线下资料](http://www.bjpowernode.com/javavideo/235.html)**

![示例图片](https://github.com/DragonLog/mutiProcessDownloader/blob/main/pictureForExample/show.png?raw=true)