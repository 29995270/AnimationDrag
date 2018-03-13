# AnimationDrag
当我在写自定义View的时候,尤其是带有拖拽变形/位移等效果的自定义View,总会结合动画一起实现,有时动画和手势动效是完全一致的.  
属性动画很简单,但手势动效却很麻烦,于是我就萌生了像定义属性动画一样,定义手势动效的想法.于是就有了这么一个库.  
**这个库还有很多问题,还不能用于实际生产**  

## 使用起来像这样
```java
        View view = findViewById(R.id.view);
        CustomFrameLayout root = (CustomFrameLayout) findViewById(R.id.root); //用以实现功能的最外层布局(仅仅处理了touch事件,可通过内部helper拓展自己的跟布局)

        AnimSet animSet = new AnimSet(Utils.getScreenWidth(this) / 4, this); //创建动画集合,定义行程距离
        animSet.setActiveHandler(new AnimSet.DefaultHandle(view));  //指定手势响应者

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationX", 0, Utils.getScreenWidth(this) / 4);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.7f);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.7f);

        animSet.oneStepAnimators(300, animator1, animator2, animator3);  //一步动画, 指定duration
        
        root.addAnimSet(animSet, AnimationDragHelper.DRAG_HORIZONTAL_L2R); //添加动画集合,并指定手势方向
```
![](https://github.com/29995270/AnimationDrag/blob/master/art/qq.gif "qq")   
  
  当然,我们也可以在4个方向上都加上手势动画  

![](https://github.com/29995270/AnimationDrag/blob/master/art/qq2.gif "qq2")  

更复杂的用法:多个步骤的手势  
```java
        AnimSet animSet = new AnimSet(maxDragDistance, this);
        animSet.setActiveHandler(new AnimSet.DefaultHandle(view));

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationY", 0, maxDragDistance/2);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "translationX", 0, Utils.getScreenWidth(this)/4);
        ObjectAnimator animator3 = ObjectAnimator.ofFloat(list, "translationY", 0, listMaxDragDistance/2);

        animSet.startAnimators(200, animator1, animator2, animator3);

        ObjectAnimator animator4 = ObjectAnimator.ofFloat(view, "translationY", maxDragDistance/2, maxDragDistance);
        ObjectAnimator animator5 = ObjectAnimator.ofFloat(list, "translationY", listMaxDragDistance/2, listMaxDragDistance);
        ObjectAnimator animator6 = ObjectAnimator.ofFloat(view, "scaleX", 1, 0.5f);
        ObjectAnimator animator7 = ObjectAnimator.ofFloat(view, "scaleY", 1, 0.5f);

        animSet.afterAnimators(true, 200, animator4, animator5, animator6, animator7);

        ObjectAnimator animator8 = ObjectAnimator.ofFloat(view, "rotationX", 0, (float) (-Math.PI * 3));
        animSet.afterAnimators(false, 100, animator8);
```  
看起来像youtube的小窗效果      
 
![](https://github.com/29995270/AnimationDrag/blob/master/art/youtube.gif "youtube")      


看上去十分美好,但是事实很残酷,api21及以下时,属性动画的reverse方法并不能从当前运行时间开始反向执行,这也使这个项目有一年没有更新任何代码,现在来看,这依旧是个很棘手的问题,还望有大佬不吝赐教.
