package iitc.triangulation.aspect;

/**
 * @author epavlova
 * @version 06.06.2016
 */
public aspect ValuesInjectingAspect {
    pointcut onNew() : initialization(@iitc.triangulation.aspect.HasValues *.new(..));

    after(): onNew() {
        System.out.println("NEW   : here!! " + thisJoinPoint);
        ValueInjector.INSTANCE.inject(thisJoinPoint.getThis());

    }

    pointcut onStatic() : @annotation(iitc.triangulation.aspect.HasValues) && staticinitialization(*);

    after(): onStatic() {
        System.out.println("STATIC: here, " + thisJoinPointStaticPart.getSignature().getDeclaringType());
        ValueInjector.INSTANCE.injectStatic(thisJoinPointStaticPart.getSignature().getDeclaringType());
    }
}
