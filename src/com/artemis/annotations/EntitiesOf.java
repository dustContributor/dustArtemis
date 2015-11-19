package com.artemis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artemis.EntitySystem;

/**
 * 
 * @author dustContributor
 */
@Documented
@Target ( ElementType.FIELD )
@Retention ( RetentionPolicy.RUNTIME )
public @interface EntitiesOf
{
	Class<? extends EntitySystem> value();
}
