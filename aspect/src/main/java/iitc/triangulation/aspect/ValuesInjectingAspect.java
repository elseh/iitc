package iitc.triangulation.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ValuesInjectingAspect {
    @Pointcut("initialization(@iitc.triangulation.aspect.HasValues *.new(..))")
    public void onNew() {}

    @Pointcut("@annotation(iitc.triangulation.aspect.HasValues) && staticinitialization(*)")
    public void onStatic() {}

    @After("onNew()")
    public void afterNew(JoinPoint joinPoint)
    {
        System.out.println("NEW   : here!! " + joinPoint);
        ValueInjector.INSTANCE.inject(joinPoint.getThis());
    }

    @After("onStatic()")
    public void afterStatic(JoinPoint.StaticPart joinPoint) {
        System.out.println("STATIC: here, " + joinPoint.getSignature().getDeclaringType());
        ValueInjector.INSTANCE.injectStatic(joinPoint.getSignature().getDeclaringType());
    }
}
