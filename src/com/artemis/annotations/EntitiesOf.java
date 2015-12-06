package com.artemis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.artemis.EntityObserver;
import com.artemis.EntitySystem;
import com.artemis.utils.ImmutableIntBag;

/**
 * This annotation is used to mark an {@link ImmutableIntBag} field of an
 * {@link EntityObserver} as being sourced from the entity list of another
 * {@link EntitySystem}. For example: <br>
 * 
 * <pre>
 * &#064;EntitiesOf ( MovementSystem.class )
 * private ImmutableIntBag moveableEntities;
 * </pre>
 * 
 * <br>
 * moveableEntities field will be injected with MovementSystem's 'active' entity
 * list at runtime. <br>
 * 
 * @author dustContributor
 */
@Documented
@Target ( ElementType.FIELD )
@Retention ( RetentionPolicy.RUNTIME )
public @interface EntitiesOf
{
	/**
	 * This is the class of the {@link EntitySystem} from which the active
	 * entity list will be sourced.
	 * 
	 * @return {@link EntitySystem} class from which the entity list will be
	 *         sourced.
	 */
	Class<? extends EntitySystem> value();
}
