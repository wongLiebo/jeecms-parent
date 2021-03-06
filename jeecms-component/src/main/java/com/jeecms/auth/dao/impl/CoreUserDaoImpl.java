/**
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.auth.dao.impl;

import com.jeecms.auth.dao.ext.CoreUserDaoExt;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.domain.querydsl.QCoreUser;
import com.jeecms.common.base.dao.BaseDao;
import com.jeecms.common.jpa.QuerydslUtils;
import com.jeecms.common.page.Paginable;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户/会员Dao实现类
 * 
 * @author: tom
 * @date: 2019年3月5日 下午4:48:37
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved.Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class CoreUserDaoImpl extends BaseDao<CoreUser> implements CoreUserDaoExt {

	/** 使用JPA底层分页Demo **/
	@SuppressWarnings("deprecation")
	public Page<CoreUser> getCoreUserPage(Map<String, Object> param, Pageable pageable) {
		List<Predicate> criteria = new ArrayList<Predicate>();
		QCoreUser qCoreUser = QCoreUser.coreUser;
		String usernameKey = "username";
		String startTimeKey = "startTime";
		String endTimeKey = "endTime";
		String enabledKey = "enabled";
		if (param.get(usernameKey) != null) {
			String username = param.get("username").toString();
			criteria.add(qCoreUser.username.eq(username));
		}
		if (param.get(startTimeKey) != null && param.get(endTimeKey) != null) {
			String startTime = param.get("startTime").toString();
			String endTime = param.get("endTime").toString();
			criteria.add(qCoreUser.createTime.between(new Date(startTime), new Date(endTime)));
		}
		if (param.get(enabledKey) != null) {
			Boolean enabled = (Boolean) param.get("enabled");
			criteria.add(qCoreUser.enabled.eq(enabled));
		}
		JPAQuery<CoreUser> query = super.getJpaQueryFactory().selectFrom(QCoreUser.coreUser);
		query.where(criteria.toArray(new Predicate[criteria.size()]));
		query.orderBy(qCoreUser.id.desc());
		Page<CoreUser> list = super.getPageByDsl(query, qCoreUser, pageable);
		return list;
	}

	@Override
	public Page<CoreUser> pageUser(Boolean enabled, List<Integer> orgids, List<Integer> roleids,
			String key, Boolean isAdmin, Short checkStatus, Integer groupId, Integer levelId, 
			Integer userSecretId, List<Integer> sourceSiteId, Pageable pageable) {
		QCoreUser qCoreUser = QCoreUser.coreUser;
		JPAQuery<CoreUser> query = getQuery(qCoreUser, enabled, orgids, roleids, key, isAdmin, 
				checkStatus,
				groupId, levelId,userSecretId, sourceSiteId);
		return QuerydslUtils.page(query, pageable, qCoreUser);
	}

	private JPAQuery<CoreUser> getQuery(QCoreUser qCoreUser, Boolean enabled, List<Integer> orgids,
			List<Integer> roleids, String key, Boolean isAdmin, Short checkStatus, Integer groupId, 
			Integer levelId,Integer userSecretId, List<Integer> sourceSiteId) {
		BooleanBuilder builder = new BooleanBuilder();
		if (enabled != null) {
			builder.and(qCoreUser.enabled.eq(enabled));
		}
		if (orgids != null && !orgids.isEmpty()) {
			builder.and(qCoreUser.org.id.in(orgids));
		}
		if (roleids != null && !roleids.isEmpty()) {
			builder.and(qCoreUser.roles.any().id.in(roleids));
		}
		if (StringUtils.isNotBlank(key)) {
			builder.and(qCoreUser.username.like("%" + key + "%")
					.or(qCoreUser.email.like("%" + key + "%"))
					.or(qCoreUser.telephone.like("%" + key + "%"))
					.or(qCoreUser.userExt.realname.like("%" + key + "%"))
					.or(qCoreUser.userExt.linephone.like("%" + key + "%")));
		}
		if (isAdmin != null) {
			builder.and(qCoreUser.admin.eq(isAdmin));
		}
		if (checkStatus != null) {
			builder.and(qCoreUser.checkStatus.eq(checkStatus));
		} else {
			builder.andNot(qCoreUser.checkStatus.eq(CoreUser.AUDIT_USER_STATUS_PASS));
		}
		if (groupId != null) {
			builder.and(qCoreUser.groupId.eq(groupId));
		}
		if (levelId != null) {
			builder.and(qCoreUser.levelId.eq(levelId));
		}
		if (userSecretId != null) {
			builder.and(qCoreUser.userSecretId.eq(userSecretId));
		}
		//来源站点
		if (sourceSiteId != null) {
			builder.and(qCoreUser.sourceSiteId.in(sourceSiteId));
		}
		builder.and(qCoreUser.hasDeleted.eq(false));
		JPAQuery<CoreUser> query = super.getJpaQueryFactory().selectFrom(qCoreUser);
		query.where(builder).orderBy(qCoreUser.checkStatus.asc()).orderBy(qCoreUser.createTime.desc());
		return query;
	}

	@Override
	public List<CoreUser> findList(Boolean enabled, List<Integer> orgid, List<Integer> roleid,
			String key, Boolean isAdmin, Short checkStatus, Integer groupId, Integer levelId,
			Integer secretId, List<Integer> sourceSiteIds,
			Paginable paginable) {
		QCoreUser qCoreUser = QCoreUser.coreUser;
		JPAQuery<CoreUser> query = getQuery(qCoreUser, enabled, orgid, roleid, key, isAdmin, 
				checkStatus, groupId,
				levelId, secretId, sourceSiteIds);
		return QuerydslUtils.list(query, paginable, qCoreUser);
	}
	
	@Override
	public Page<CoreUser> pageWechat(Integer orgId, Integer roleid, String username,
			Pageable pageable, List<Integer> notIds) {
		QCoreUser coreUser = QCoreUser.coreUser;
		BooleanBuilder builder = new BooleanBuilder();
		if (orgId != null) {
			builder.and(coreUser.orgId.eq(orgId));
		}
		if (roleid != null) {
			builder.and(coreUser.roles.any().id.in(roleid));
		}
		if (username != null) {
			builder.and(coreUser.username.like("%" + username + "%")
					.or(coreUser.userExt.realname.like("%" + username + "%")));
		}
		if (notIds != null && notIds.size() > 0) {
			builder.and(coreUser.id.notIn(notIds));
		}
		builder.and(coreUser.admin.eq(true));
		builder.and(coreUser.hasDeleted.eq(false));
		JPAQuery<CoreUser> query = super.getJpaQueryFactory().selectFrom(coreUser);
		query.where(builder).orderBy(coreUser.createTime.desc());
		return QuerydslUtils.page(query, pageable, coreUser);
	}

	@Override
	public List<CoreUser> listWechat(List<Integer> ids, Integer orgId, Integer roleid, String username) {
		QCoreUser coreUser = QCoreUser.coreUser;
		BooleanBuilder builder = new BooleanBuilder();
		if (ids != null && ids.size() > 0) {
			builder.and(coreUser.id.in(ids));
		} else {
			return null;
		}
		if (orgId != null) {
			builder.and(coreUser.orgId.eq(orgId));
		}
		if (roleid != null) {
			builder.and(coreUser.roles.any().id.in(roleid));
		}
		if (username != null) {
			builder.and(coreUser.username.like("%" + username + "%")
					.or(coreUser.userExt.realname.like("%" + username + "%")));
		}
		builder.and(coreUser.admin.eq(true));
		builder.and(coreUser.hasDeleted.eq(false));
		return super.getJpaQueryFactory().selectFrom(coreUser).where(builder)
				.orderBy(coreUser.createTime.desc()).fetch();
	}


	@Override
	public long getUserSum(Date beginTime, Date endTime, Integer siteId, Short checkStatus) {
		QCoreUser qCoreUser = QCoreUser.coreUser;
		BooleanBuilder builder = new BooleanBuilder();
		if (beginTime != null) {
			builder.and(qCoreUser.createTime.goe(beginTime));
		}
		if (endTime != null) {
			builder.and(qCoreUser.createTime.loe(endTime));
		}
		if (siteId != null) {
			builder.and(qCoreUser.sourceSiteId.eq(siteId));
		}
		if (checkStatus != null) {
			builder.and(qCoreUser.checkStatus.eq(checkStatus));
		}
		builder.and(qCoreUser.admin.eq(false));
		JPAQuery<CoreUser> jpaQuery = new JPAQuery<CoreUser>(this.em);
		//增加使用查询缓存
		jpaQuery.setHint(QueryHints.HINT_CACHEABLE, true);
		return jpaQuery.from(qCoreUser).where(builder).fetchCount();
	}

    @Override
    public long getSafeOrAuditUser(List<Integer> ids, Integer status) {
	    QCoreUser qCoreUser = QCoreUser.coreUser;
        BooleanBuilder builder = new BooleanBuilder();
        if (!CollectionUtils.isEmpty(ids)) {
            builder.and(qCoreUser.id.in(ids));
        }
        switch (status) {
            case CoreUser.USER_SAFETY_ADMIN:
                builder.and(qCoreUser.safetyAdmin.eq(true));
                break;
            case CoreUser.USER_AUDIT_ADMIN:
                builder.and(qCoreUser.auditAdmin.eq(true));
                break;
            default:
                break;
        }

        builder.and(qCoreUser.admin.eq(true));
        builder.and(qCoreUser.hasDeleted.eq(false));
        JPAQuery<CoreUser> jpaQuery = new JPAQuery<CoreUser>(this.em);
        //增加使用查询缓存
        jpaQuery.setHint(QueryHints.HINT_CACHEABLE, true);
        return jpaQuery.from(qCoreUser).where(builder).fetchCount();
    }

    @Override
    public List<CoreUser> getSafeOrAuditUser(Integer status,Boolean enabled) {
        QCoreUser qCoreUser = QCoreUser.coreUser;
        BooleanBuilder builder = new BooleanBuilder();
        switch (status) {
            case CoreUser.USER_SAFETY_ADMIN:
                builder.and(qCoreUser.safetyAdmin.eq(true));
                break;
            case CoreUser.USER_AUDIT_ADMIN:
                builder.and(qCoreUser.auditAdmin.eq(true));
                break;
            default:
                break;
        }
        builder.and(qCoreUser.admin.eq(true));
        builder.and(qCoreUser.hasDeleted.eq(false));
        if (enabled != null) {
            builder.and(qCoreUser.enabled.eq(enabled));
        }
        JPAQuery<CoreUser> jpaQuery = new JPAQuery<CoreUser>(this.em);
        //增加使用查询缓存
        jpaQuery.setHint(QueryHints.HINT_CACHEABLE, true);
        return jpaQuery.from(qCoreUser).where(builder).fetch();
    }

    @Override
    public List<Integer> getSafeOrAuditUserIds(Integer status) {
        QCoreUser qCoreUser = QCoreUser.coreUser;
        BooleanBuilder builder = new BooleanBuilder();
        switch (status) {
            case CoreUser.USER_SAFETY_ADMIN:
                builder.and(qCoreUser.safetyAdmin.eq(true));
                break;
            case CoreUser.USER_AUDIT_ADMIN:
                builder.and(qCoreUser.auditAdmin.eq(true));
                break;
            default:
                break;
        }
        builder.and(qCoreUser.admin.eq(true));
        builder.and(qCoreUser.hasDeleted.eq(false));
        JPAQuery<CoreUser> jpaQuery = new JPAQuery<CoreUser>(this.em);
        //增加使用查询缓存
        jpaQuery.setHint(QueryHints.HINT_CACHEABLE, true);
        return jpaQuery.select(qCoreUser.id).from(qCoreUser).where(builder).fetch();
    }

    @Override
    public Page<CoreUser> pageSafeManageUser(List<Integer> orgids, List<Integer> roleids, String key,  List<Integer> notIds, Pageable pageable) {
        QCoreUser qCoreUser = QCoreUser.coreUser;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qCoreUser.hasDeleted.eq(false));
        builder.and(qCoreUser.admin.eq(true));
        if (!CollectionUtils.isEmpty(orgids)) {
            builder.and(qCoreUser.org.id.in(orgids));
        }
        if (!CollectionUtils.isEmpty(roleids)){
            builder.and(qCoreUser.roles.any().id.in(roleids));
        }
        if (StringUtils.isNotBlank(key)) {
            builder.and(qCoreUser.username.like("%" + key + "%")
                    .or(qCoreUser.userExt.realname.like("%" + key + "%")));
        }
        if (!CollectionUtils.isEmpty(notIds)) {
            builder.and(qCoreUser.id.notIn(notIds));
        }
        JPAQuery<CoreUser> query = super.getJpaQueryFactory().selectFrom(qCoreUser).where(builder).orderBy(qCoreUser.createTime.desc());
        return QuerydslUtils.page(query, pageable, qCoreUser);
    }

	@Override
	public Page<CoreUser> pageReinsurance(boolean set, Boolean enabled, List<Integer> orgIds, List<Integer> roleIds,
										  String key, Integer userSecretId, Pageable pageable) {
		QCoreUser qCoreUser = QCoreUser.coreUser;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(qCoreUser.hasDeleted.eq(false));
		builder.and(qCoreUser.admin.eq(true));
		if (set) {
			builder.and(qCoreUser.userSecretId.isNotNull());
			if (userSecretId != null) {
				builder.and(qCoreUser.userSecretId.eq(userSecretId));
			}
		} else {
			builder.and(qCoreUser.userSecretId.isNull());
		}
		if (enabled != null) {
			builder.and(qCoreUser.enabled.eq(enabled));
		}
		if (!CollectionUtils.isEmpty(orgIds)) {
			builder.and(qCoreUser.org.id.in(orgIds));
		}
		if (!CollectionUtils.isEmpty(roleIds)) {
			builder.and(qCoreUser.roles.any().id.in(roleIds));
		}
		if (StringUtils.isNotBlank(key)) {
			builder.and(qCoreUser.username.like("%" + key + "%")
					.or(qCoreUser.email.like("%" + key + "%"))
					.or(qCoreUser.telephone.like("%" + key + "%"))
					.or(qCoreUser.userExt.realname.like("%" + key + "%"))
					.or(qCoreUser.userExt.linephone.like("%" + key + "%")));
		}
		JPAQuery<CoreUser> query = super.getJpaQueryFactory().selectFrom(qCoreUser).where(builder).orderBy(qCoreUser.createTime.desc());
		return QuerydslUtils.page(query, pageable, qCoreUser);
	}

    @Override
    public List<CoreUser> getReinsuranceSafeOrAuditUser(Integer status, Boolean enabled) {
        QCoreUser qCoreUser = QCoreUser.coreUser;
        BooleanBuilder builder = new BooleanBuilder();
        switch (status) {
            case CoreUser.REINSURANCE_SAFETY_ADMIN:
                builder.and(qCoreUser.safetyReinsuranceAdmin.eq(true));
                break;
            case CoreUser.REINSURANCE_AUDIT_ADMIN:
                builder.and(qCoreUser.auditReinsuranceAdmin.eq(true));
                break;
            default:
                break;
        }
        builder.and(qCoreUser.admin.eq(true));
        builder.and(qCoreUser.hasDeleted.eq(false));
        if (enabled != null) {
            builder.and(qCoreUser.enabled.eq(enabled));
        }
        JPAQuery<CoreUser> jpaQuery = new JPAQuery<CoreUser>(this.em);
        //增加使用查询缓存
        jpaQuery.setHint(QueryHints.HINT_CACHEABLE, true);
        return jpaQuery.from(qCoreUser).where(builder).fetch();
    }

	private EntityManager em;

	@javax.persistence.PersistenceContext
	public void setEm(EntityManager em) {
		this.em = em;
	}
}
