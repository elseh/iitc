package iitc.triangulation.other;

import iitc.triangulation.aspect.ValueInjector;

/**
 * @author epavlova
 * @version 08.06.2016
 */
public aspect SimpleAspect {
    /*pointcut someMain(): call(void *.initAll(..));
    pointcut someAnnot(): @annotation(iitc.triangulation.aspect.HasValues);

    after() : someAnnot() {
        System.out.println("HasValues or something");
        System.out.println(thisJoinPoint);
        System.out.println(thisJoinPoint.getStaticPart().getSignature().getDeclaringType());
        System.out.println(thisJoinPoint.getTarget());
        ValueInjector.INSTANCE.injectStatic(thisJoinPoint.getStaticPart().getSignature().getDeclaringType());
    }

    before() : someMain() {
        System.out.println("HERE!!!!!");
        System.out.println(thisJoinPoint);

    }*/
}
