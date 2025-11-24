package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     *  前置通知，在方法执行前执行
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始进行数据填充……");
        //获取当前被拦截的Mapper方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 获取方法签名
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);// 获取注解对象
        OperationType operationType = autoFill.value(); // 获取数据库操作类型
        // 获取方法参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null && args.length == 0) {
            return;
        }
        Object entity = args[0]; // 获取实体对象(默认第一个参数为实体对象)
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据对应的数据库操作类型，为对应的属性赋值
        if (operationType == OperationType.INSERT) {
            Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);

            //赋值
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);
            setCreateTime.invoke(entity, now);
            setCreateUser.invoke(entity, currentId);
        } else if (operationType == OperationType.UPDATE) {
            Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

            //赋值
            setUpdateTime.invoke(entity, now);
            setUpdateUser.invoke(entity, currentId);

        }
    }
}
