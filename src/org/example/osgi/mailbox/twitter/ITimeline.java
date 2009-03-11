/*******************************************************************************
 * Copyright (c) 2009 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package org.example.osgi.mailbox.twitter;

import java.util.Collection;

import org.example.osgi.mailbox.api.Message;

import twitter4j.TwitterException;

/**
 * @author Neil Bartlett
 *
 */
public interface ITimeline {
	long getInitialTimeline(Collection<? super Message> into) throws TwitterException;
	
	long getTimelineSinceId(long sinceId, Collection<? super Message> into) throws TwitterException;
}
