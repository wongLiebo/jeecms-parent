package com.jeecms.interact.dao;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.dao.IBaseDao;
import com.jeecms.interact.dao.ext.CmsFormDaoExt;
import com.jeecms.interact.domain.CmsFormEntity;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * 表单dao
 * @author: tom
 * @date:
 */
public interface CmsFormDao extends IBaseDao<CmsFormEntity,Integer>, CmsFormDaoExt {
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    List<CmsFormEntity> findByTitleAndFormSceneAndHasDeletedAndSiteId(String title, Short formScene, Boolean hasDelete, Integer siteId);


}
