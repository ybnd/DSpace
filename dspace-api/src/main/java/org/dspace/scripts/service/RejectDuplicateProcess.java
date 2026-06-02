/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.scripts.service;

import javax.persistence.PersistenceException;

/**
 * This exception is thrown when we reject a Process because it is a duplicate of an existing Process.
 * <br>
 * This prevents accidentally starting the same exact Process multiple times, and happens when:
 * <ul>
 *     <li>The <code>script</code> and <code>parameters</code> are identical</li>
 *     <li>The <code>start_date</code> is within 1 second</li>
 * </ul>
 * In this case, the first Process will be created, but subsequent Processes will not.
 * <br>
 * Mapped to HTTP 409 (Conflict) in the DSpace REST API.
 */
public class RejectDuplicateProcess extends PersistenceException {
    public RejectDuplicateProcess(Throwable t) {
        super(t);
    }
}
