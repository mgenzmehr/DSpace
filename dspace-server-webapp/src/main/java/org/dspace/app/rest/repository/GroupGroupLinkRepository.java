/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.dspace.app.rest.model.GroupRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Link repository for "groups" subresource of an individual group.
 */
@Component(GroupRest.CATEGORY + "." + GroupRest.PLURAL_NAME + "." + GroupRest.SUBGROUPS)
public class GroupGroupLinkRepository extends AbstractDSpaceRestRepository
        implements LinkRestRepository {

    @Autowired
    GroupService groupService;

    @PreAuthorize("hasPermission(#groupId, 'GROUP', 'READ')")
    public Page<GroupRest> getGroups(@Nullable HttpServletRequest request,
                                     UUID groupId,
                                     @Nullable Pageable optionalPageable,
                                     Projection projection) {
        try {
            Context context = obtainContext();
            Group group = groupService.find(context, groupId);
            if (group == null) {
                throw new ResourceNotFoundException("No such group: " + groupId);
            }
            int total = groupService.countByParent(context, group);
            Pageable pageable = utils.getPageable(optionalPageable);
            List<Group> memberGroups = groupService.findByParent(context, group, pageable.getPageSize(),
                                                                Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(memberGroups, pageable, total, projection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
