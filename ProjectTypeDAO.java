package com.chrysler.rp.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.chrysler.rp.App;
import com.chrysler.rp.AppConstants;
import com.chrysler.rp.gxt.server.RPServiceImplHelper;
import com.chrysler.rp.gxt.server.ValidateUser;
import com.chrysler.rp.gxt.shared.BaseException;
import com.chrysler.rp.gxt.shared.UserData;
import com.chrysler.rp.model.ProjectType;
import com.chrysler.rp.model.User;

public class ProjectTypeDAO {
	private static ProjectTypeDAO instance;

	static {
		instance = new ProjectTypeDAO();
	}

	public static ProjectTypeDAO getInstance() {
		return instance;
	}

	public ProjectTypeDAO() {
	}

	// Added by - T9434GB - ENHC0060944 -Shared Restriction for Non-NAFTA Admins - CN 2 -Start-END
	public List<ProjectType> findAll(Session session, UserData user, String selectedUserRegion,boolean isMyHoursScreen) throws BaseException {
		Criteria criteria = session.createCriteria(ProjectType.class);
		RPServiceImplHelper helper = new RPServiceImplHelper();
		//condition changes as per changes made as per ENHC0121562 by t9281ec
		if ("".equalsIgnoreCase(selectedUserRegion) && !(user.isItSuperAdmin() || user.isGlobalAdmin()) && user.getUSER_REGION().equalsIgnoreCase("N")) {
			criteria.add(Restrictions.not((Restrictions.eq("projectTypeRegion", "A"))));
			criteria.add(Restrictions.not((Restrictions.eq("projectTypeRegion", "I"))));
			criteria.add(Restrictions.not((Restrictions.eq("projectTypeRegion", "C"))));
		} 
		// Added by - T9434GB - ENHC0060944 -Shared Restriction for Non-NAFTA Admins - CN 1 -Start
		/* t8274pr - Removed this restriction - fix for CHG0778225 - 3-April-2024 - start
		else if ("".equalsIgnoreCase(selectedUserRegion) && !(user.isItSuperAdmin() || user.isGlobalAdmin()) && user.getUSER_REGION().equalsIgnoreCase("A") && !isMyHoursScreen) {
			
			criteria.add(Restrictions.and(Restrictions.not((Restrictions.eq("projectTypeRegion", "N"))),
					Restrictions.not((Restrictions.eq("projectTypeRegion", "S")))));
		}
		t8274pr - Removed this restriction - fix for CHG0778225 - 3-April-2024 - end
		*/	
		// Added by - T9434GB - ENHC0060944 -Shared Restriction for Non-NAFTA Admins - CN 1 -End
		 else if ("".equalsIgnoreCase(selectedUserRegion) && !(user.isItSuperAdmin() || user.isGlobalAdmin()) && user.getUSER_REGION().equalsIgnoreCase("A")) {
			 criteria.add(Restrictions.not((Restrictions.eq("projectTypeRegion", "N"))));
			}
		 else if (!"".equalsIgnoreCase(selectedUserRegion)) {
			if (selectedUserRegion.equalsIgnoreCase("NAFTA")) {
				criteria.add(Restrictions.not((Restrictions.eq("projectTypeRegion", "A"))));
			} else if (selectedUserRegion.equalsIgnoreCase("APAC")) {
				criteria.add(Restrictions.not((Restrictions.eq("projectTypeRegion", "N"))));
			}
		}
		else if (!(user.isItSuperAdmin() || user.isGlobalAdmin()) && user.getUSER_REGION().equalsIgnoreCase("C")) {
			criteria.add(Restrictions.and(Restrictions.not((Restrictions.eq("projectTypeRegion", "N"))),
					Restrictions.not((Restrictions.eq("projectTypeRegion", "I")))));
			criteria.add(Restrictions.not(Restrictions.eq("projectTypeRegion", "S")));//added by T2145JJ for excluding shared projects from India and China 
			
		}else if (!(user.isItSuperAdmin() || user.isGlobalAdmin()) && ("I").equalsIgnoreCase(user.getUSER_REGION())) {
			criteria.add(Restrictions.and(Restrictions.not((Restrictions.eq("projectTypeRegion", "N"))),
					Restrictions.not((Restrictions.eq("projectTypeRegion", "C")))));
			criteria.add(Restrictions.not(Restrictions.eq("projectTypeRegion", "S")));//added by T2145JJ for excluding shared projects from India and China 
			
		}
		if (user.isICTUser()) {
			String[] projId = helper.getPropertyValues(App.getProperty(AppConstants.PROJECT_TYPE_IDS));
			Long[] longProjIdList = new Long[projId.length];
			int i = 0;
			for (String str : projId) {
				longProjIdList[i++] = Long.parseLong(str);
			}
			criteria.add(Restrictions.in("id", (longProjIdList)));
		}
		criteria.addOrder(Order.asc("order"));
		List<ProjectType> projectTypeList = criteria.list();
		List<ProjectType> prjListFiltered = new ArrayList<ProjectType>();
		boolean doFilter = false;
		if ("U".equalsIgnoreCase(user.getRole()) ||	"B".equalsIgnoreCase(user.getRole()) ||	"V".equalsIgnoreCase(user.getRole()) ||	"R".equalsIgnoreCase(user.getRole()) )
		{
			doFilter = true;
			ValidateUser validateUser = new ValidateUser();
			User users = null;
			try {
				users = validateUser.getUserByTid(session, user.getTid());

				if (users != null) {
				    String bucketList = users.getLocDeptName().getLocDeptBucket();
				    String[] userBuckets = bucketList.split(",");

				    for (int i = 0; i < projectTypeList.size(); i++) {
				        String[] prjBktList = projectTypeList.get(i).getBucketValue().split(",");
				        boolean matchFound = false;

				        for (String prjBkt : prjBktList) {
				            for (String userBkt : userBuckets) {
				                if (prjBkt.trim().equals(userBkt.trim())) {
				                    matchFound = true;
				                    break;
				                }
				            }
				            if (matchFound) {
				                break;
				            }
				        }

				        if (matchFound) {
				            prjListFiltered.add(projectTypeList.get(i));
				        }
				    }
				}
			} catch (Throwable e) {
				throw new BaseException("Error in : projectTypeDAO", e);
			}
		}
		if (prjListFiltered != null && doFilter) {
			return prjListFiltered;
		} else {
			return projectTypeList;
		}
	}
}