package org.sigmah.client.ui.view.project.indicator;

/*
 * #%L
 * Sigmah
 * %%
 * Copyright (C) 2010 - 2016 URD
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * CSS and images used to display indicators.
 * 
 * @author Raphaël Calabro (rcalabro@ideia.fr)
 */
public interface IndicatorResources extends ClientBundle {
	
	/**
	 * Shared instance.
	 */
	public static final IndicatorResources INSTANCE = GWT.create(IndicatorResources.class);
	
	ImageResource star();
	ImageResource emptyStar();
	ImageResource map();
	ImageResource emptyMap();
	
	@Source("IndicatorResources.css")
	Style css();
	
	public interface Style extends CssResource {
		String mapIcon();
		String emptyMapIcon();
		String starIcon();
		String emptyStarIcon();
		String indicatorLabel();
		String indicatorLabelInactive();
	}
}
