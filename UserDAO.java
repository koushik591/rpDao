package com.chrysler.rp.dao;

//Added for Serv SeRV00457164
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.chrysler.rp.UserSort;
import com.chrysler.rp.gxt.server.DTOManager;
import com.chrysler.rp.gxt.server.RPServiceImpl;
import com.chrysler.rp.gxt.shared.BaseException;
import com.chrysler.rp.gxt.shared.UIConstants;
import com.chrysler.rp.gxt.shared.UserData;
import com.chrysler.rp.model.BaseHours;
import com.chrysler.rp.model.LDAPLocDeptName;
import com.chrysler.rp.model.User;
import com.chrysler.rp.model.UserComments;
import com.chrysler.rp.model.UserCommentsCk;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;

public class UserDAO {
	
	//Added by T9371M0 for emailAll starts
	public static List<User> usersMailList = null;
	//Added by T9371M0 for emailAll ends
	
	private static UserDAO instance;
	// SeRV00465408 - t2763co start
	private static final Logger logger = LogManager.getLogger(UserDAO.class);
	private static final String USER_QUERY = "select CAST(users.I_TID AS VARCHAR(40)) AS TID,users.I_USR, users.N_FIRST ,users.N_LAST, users.T_STMP_ACTVD,"
			+ " users.T_STMP_REMV,users.D_CREATD, "
			+ "users.I_SUPVR,users.I_BKUP_APRVL,users.I_LD,users.N_EMAIL,CAST(users.C_EMPL_TYP AS VARCHAR(3)) AS C_EMPL_TYP,users.C_PH_NMBR,"
			+ "hours.C_STAT from "
			+ "RP.RP_USER users LEFT JOIN RP.RP_HOURS hours ON users.I_USR = hours.I_USR "
			+ "and hours.D_HRS between :start and :end " + "where ";

	private static final String WHERE_CLAUSE = "and (users.T_STMP_ACTVD is null or "
			+ "(users.T_STMP_ACTVD is not null and users.T_STMP_ACTVD >:end)) "
			+ "and (users.T_STMP_REMV is null or users.T_STMP_REMV>:start) "
			+ "and (users.D_CREATD <:end) ";
	private static final String SUP_CONDITION = "users.I_SUPVR =:supervisorId ";
	private static final String BKUP_CONDITION = "users.I_BKUP_APRVL =:supervisorId ";
	private static final String STATUS_CONDITION = "and hours.C_STAT =:status ";
	// SeRV00679285 - T8860SG - Starts
	private static final String EMPTYPE_CONDITION = "and users.C_EMPL_TYP = :emptype ";
	private static final String EMPTYPE_DROPDOWN_QUERY = "select CAST(users.I_TID AS VARCHAR(40)) AS TID,  users.T_STMP_ACTVD, users.T_STMP_REMV, users.D_CREATD, CAST(users.C_EMPL_TYP AS VARCHAR(10)) from RP.RP_USER users where ";
	private static final String WHERE_CLAUSE_EMPTYPE = "and (users.T_STMP_ACTVD is null or "
			+ "(users.T_STMP_ACTVD is not null and users.T_STMP_ACTVD >:end)) "
			+ "and (users.T_STMP_REMV is null or (users.T_STMP_REMV>:end and users.D_CREATD<:end) )"
			+ "and (users.D_CREATD <:end) ";
	// SeRV00679285 - T8860SG - Ends
	private static final String GROUPBY_CONDITION = "group by hours.C_STAT,users.I_TID, users.I_USR,users.N_FIRST ,users.N_LAST,"
			+ " users.T_STMP_ACTVD,users.T_STMP_REMV,users.D_CREATD, "
			+ " users.I_SUPVR,users.I_BKUP_APRVL,users.I_LD,users.N_EMAIL,users.C_EMPL_TYP,"
			+ " users.C_PH_NMBR ";
	// SeRV00465408 - t2763co end
	static {
		instance = new UserDAO();
	}

	public static UserDAO getInstance() {
		return instance;
	}

	public UserDAO() {
	}

	public PageResult<User> find(Session session, PageConfig config,
			long supervisorId,String region, boolean iTSuperAdminFlag , boolean globalAdminFlag, Date start, Date end) throws BaseException {
		Criteria criteria = session.createCriteria(User.class);//global admin flag added as per ENHC0121562 by t9281ec

		if (config.getSearchField().equals(UserData.SUPERVISOR)
				|| config.getSearchField().equals(UserData.BACKUP_APPROVAL)) {
			criteria = addSearchCriteria(criteria, UserData.TID, config
					.getSearchValue());
			// added for SeRV00649233 start
			criteria.addOrder(Order.asc("id"));
			// added for SeRV00649233 end
			List<User> users = criteria.list();
			if (users.size() < 1) {
				throw new BaseException("Can not find a user for this TID");
			}
			User su = users.get(users.size() - 1);
			criteria = session.createCriteria(User.class);
			if (config.getSearchField().equals(UserData.SUPERVISOR)) {
				criteria.add(Restrictions.eq("supervisor", su));
			}
			if (config.getSearchField().equals(UserData.BACKUP_APPROVAL)) {
				criteria.add(Restrictions.eq("backupApproval", su));
			}

			criteria.add(Restrictions.or(Restrictions
					.isNull("deactivationDate"), Restrictions.and(Restrictions
					.isNotNull("deactivationDate"), Restrictions.gt(
					"deactivationDate", end))));
			criteria.add(Restrictions.or(Restrictions.isNull("removalDate"),
					Restrictions.gt("removalDate", start)));
			criteria.add(Restrictions.le("createdDate", end));

		} else {

			if (config.getSearchField() != null
					&& config.getSearchValue() != null) {
				criteria = addSearchCriteria(criteria, config.getSearchField(),
						config.getSearchValue());
			}
			if (supervisorId != 0) {
				User supervisor = (User) session.load(User.class, supervisorId);
				if (supervisor == null) {
					throw new BaseException("Can not find a Supervisor");
				}
				criteria.add(Restrictions.or(Restrictions.eq("supervisor",
						supervisor), Restrictions.eq("backupApproval",
						supervisor)));
			}

			//TODO - Flag for Activation call
			criteria.add(Restrictions.or(Restrictions
					.isNull("deactivationDate"), Restrictions.and(Restrictions
					.isNotNull("deactivationDate"), Restrictions.gt(
					"deactivationDate", end))));
			criteria.add(Restrictions.or(Restrictions.isNull("removalDate"),
					Restrictions.gt("removalDate", start)));
			criteria.add(Restrictions.le("createdDate", end));
			
			// Add region criteria
			if(supervisorId == 0){
				//if(!iTSuperAdminFlag && region.equalsIgnoreCase("N")){
				//changes made as per ENHC0121562 by t9281ec
				if(!(iTSuperAdminFlag || globalAdminFlag) && region.equalsIgnoreCase("N")){
					criteria.add(Restrictions.eq("userRegion", region));
				}
				//Commented and edited by T7166PA--Starts
//				else if (!iTSuperAdminFlag && region.equalsIgnoreCase("A")) {
//					criteria.add(Restrictions.eq("userRegion", region));
//				}
				// Added by T9434GB - Search user in user manager tab - start
				else if (!(iTSuperAdminFlag || globalAdminFlag) && (region.equalsIgnoreCase("A"))) {
					criteria.add(Restrictions.ne("userRegion", "N"));
				}
				else if (!(iTSuperAdminFlag || globalAdminFlag) && (region.equalsIgnoreCase("C")||
						region.equalsIgnoreCase("I"))) {
					criteria.add(Restrictions.eq("userRegion", region));
				}
				// Added by T9434GB - Search user in user manager tab - end
				//Commented and edited by T7166PA--Ends
			}
			

		}
		//added by t5317sk	
		if(config.getSearchField().equals(UserData.JOBCODETITLE)){
			criteria = addSearchCriteria(criteria, UserData.JOBCODETITLE, config.getSearchValue());
		}
		
		else if(config.getSearchField().equals(UserData.CID)){
			criteria = addSearchCriteria(criteria, UserData.CID, config.getSearchValue());
		}
		//end by t5317sk
		//Added by T9371M0 for emailAll starts
//		usersMailList = criteria.list();
		//Added by T9371M0 for emailAll ends
		
//		criteria.setMaxResults(60);
		ScrollableResults scr = criteria.scroll();
		scr.last();
		int totalSize = scr.getRowNumber() + 1;

		if (config.getSortField() != null) {
			if (config.isAscending()) {
				// added for SeRV00649233 start
				if (config.getSearchValue() != null
						&& !"".equals(config.getSearchValue().trim())
						&& config.getSearchField().equals(UserData.FIRST_NAME)
						&& config.getSortField().equals(UserData.FIRST_NAME)) {
					criteria.addOrder(
							Order.asc(UserData.FIRST_NAME).ignoreCase())
							.addOrder(
									Order.asc(UserData.LAST_NAME).ignoreCase());
				} else if (config.getSearchValue() != null
						&& !"".equals(config.getSearchValue().trim())
						&& config.getSearchField().equals(UserData.LAST_NAME)
						&& config.getSortField().equals(UserData.LAST_NAME)) {
					criteria
							.addOrder(
									Order.asc(UserData.LAST_NAME).ignoreCase())
							.addOrder(
									Order.asc(UserData.FIRST_NAME).ignoreCase());
				} else {
					// added for SeRV00649233 end
					// criteria.addOrder(Order.asc(config.getSortField()));
					criteria.addOrder(Order.asc(config.getSortField())
							.ignoreCase());
					// added for SeRV00649233 start
				}
				// added for SeRV00649233 end
			} else {
				// added for SeRV00649233 start
				if (config.getSortField().equals(UserData.FIRST_NAME)) {
					criteria
							.addOrder(
									Order.desc(UserData.FIRST_NAME)
											.ignoreCase())
							.addOrder(
									Order.desc(UserData.LAST_NAME).ignoreCase());
				} else if (config.getSortField().equals(UserData.LAST_NAME)) {
					criteria.addOrder(
							Order.desc(UserData.LAST_NAME).ignoreCase())
							.addOrder(
									Order.desc(UserData.FIRST_NAME)
											.ignoreCase());
				} else {
					// added for SeRV00649233 end
					// criteria.addOrder(Order.desc(config.getSortField()));
					criteria.addOrder(Order.desc(config.getSortField())
							.ignoreCase());
					// added for SeRV00649233 start
				}
				// added for SeRV00649233 end
			}
		}

		// Added for Serv SeRV00457164
		List<User> users = null;
		if ( supervisorId == 0) {
			// Serv SeRV00457164 end
			criteria.setFirstResult(config.getOffset());
			// criteria.setMaxResults(config.getLimit() + 5);
			criteria.setMaxResults(config.getLimit());

			// criteria.addOrder(Order.asc("id"));//commented for SeRV00649233
			// Added for Serv SeRV00457164
			// List<User> users = criteria.list();
			
			// RP-APAC
				
				//if(!iTSuperAdminFlag && region.equalsIgnoreCase("N")){
			//changes made as per ENHC0121562 by t9281ec
			if(!(iTSuperAdminFlag || globalAdminFlag) && region.equalsIgnoreCase("N")){
					criteria.add(Restrictions.eq("userRegion", region));
				}
				//Commented and edited by T7166PA--Starts
//				else if (!iTSuperAdminFlag && region.equalsIgnoreCase("A")) {
//					criteria.add(Restrictions.eq("userRegion", region));
//				}
				// Added by T9434GB - Search user in user manager tab - start
				else if (!(iTSuperAdminFlag || globalAdminFlag) && (region.equalsIgnoreCase("A"))) {
					criteria.add(Restrictions.ne("userRegion", "N"));
				}
				else if (!(iTSuperAdminFlag || globalAdminFlag) && (region.equalsIgnoreCase("C")||
						region.equalsIgnoreCase("I"))) {
					criteria.add(Restrictions.eq("userRegion", region));
				}
				// Added by T9434GB - Search user in user manager tab - end
				//Commented and edited by T7166PA--Ends
				users = criteria.list();
		
			
			
			
		}else {
			users = criteria.list();
			if (users != null && !users.isEmpty()) {
				List<User> supervisorList = new ArrayList<User>();
				List<User> nonSupervisorList = new ArrayList<User>();
				UserSort.getSplitList(users, supervisorList, nonSupervisorList,
						supervisorId);
				Collections.sort(nonSupervisorList, new UserSort());
				users = new ArrayList<User>();
				users.addAll(supervisorList);
				users.addAll(nonSupervisorList);
				List<User> pageList = new ArrayList<User>();
				int maxLimit = config.getOffset() + config.getLimit();
				if (maxLimit < totalSize) {
					pageList = users.subList(config.getOffset(), maxLimit);
				} else {
					pageList = users.subList(config.getOffset(), totalSize);
				}
				users = pageList;
			}

		}
		// Serv SeRV00457164 end

		// added for SeRV00299944
		for (int x = 0; x < users.size(); x++) {
			String tid = users.get(x).getTid();
			double baseHours = getBaseHours(session, tid, start);
			users.get(x).setActualBaseHours(baseHours);
			// for comment SeRV
			ArrayList commentWithStat = getUserComments(session, tid, start);
			users.get(x).setUserComments(commentWithStat.get(0).toString());
			users.get(x).setCommentStatus(commentWithStat.get(1).toString());
			// end comment SeRV

		}
		// end SeRV0029944
		return new PageResult<User>(users, totalSize);
	}
	
	/**
	 * Method created for quick load of User Manager screen
	 * 	SeRV01016061 - T8860SG
	 * @param session
	 * @param searchField
	 * @param searchValue
	 * @param supervisorId
	 * @param region
	 * @param iTSuperAdminFlag
	 * @param start
	 * @param end
	 * @return
	 * @throws BaseException
	 */
	public List findUserEmailAll(Session session, String searchField, String searchValue,
			long supervisorId,String region, boolean iTSuperAdminFlag , boolean globalAdminFlag,Date start, Date end) throws BaseException {
		
		logger.info("Enter - UserDAO findUserEmailAll");
		Criteria criteria = session.createCriteria(User.class);
		searchField = DTOManager.getUserFieldName(searchField);
		if (searchField.equals(UserData.SUPERVISOR)
				|| searchField.equals(UserData.BACKUP_APPROVAL)) {
			criteria = addSearchCriteria(criteria, UserData.TID, searchValue);
			// added for SeRV00649233 start
			criteria.addOrder(Order.asc("id"));
			// added for SeRV00649233 end
			List<User> users = criteria.list();
			if (users.size() < 1) {
				throw new BaseException("Can not find a user to email for selected search criteria");
			}
			User su = users.get(users.size() - 1);
			criteria = session.createCriteria(User.class);
			if (searchField.equals(UserData.SUPERVISOR)) {
				criteria.add(Restrictions.eq("supervisor", su));
			}
			if (searchField.equals(UserData.BACKUP_APPROVAL)) {
				criteria.add(Restrictions.eq("backupApproval", su));
			}

			criteria.add(Restrictions.or(Restrictions
					.isNull("deactivationDate"), Restrictions.and(Restrictions
					.isNotNull("deactivationDate"), Restrictions.gt(
					"deactivationDate", end))));
			criteria.add(Restrictions.or(Restrictions.isNull("removalDate"),
					Restrictions.gt("removalDate", start)));
			criteria.add(Restrictions.le("createdDate", end));

		} else {
				if (searchField != null
						&& searchValue != null) {

					criteria = addSearchCriteria(criteria, searchField,
							searchValue);
				}
				if (supervisorId != 0) {
					User supervisor = (User) session.load(User.class, supervisorId);
					if (supervisor == null) {
						throw new BaseException("Can not find a Supervisor");
					}
					criteria.add(Restrictions.or(Restrictions.eq("supervisor",
							supervisor), Restrictions.eq("backupApproval",
							supervisor)));
				}
				criteria.add(Restrictions.or(Restrictions
						.isNull("deactivationDate"), Restrictions.and(Restrictions
						.isNotNull("deactivationDate"), Restrictions.gt(
						"deactivationDate", end))));
				criteria.add(Restrictions.or(Restrictions.isNull("removalDate"),
						Restrictions.gt("removalDate", start)));
				criteria.add(Restrictions.le("createdDate", end));
				
				// Add region criteria
				if(supervisorId == 0){
					if(!(iTSuperAdminFlag || globalAdminFlag) && region.equalsIgnoreCase("N")){
						criteria.add(Restrictions.eq("userRegion", region));
					}
					//Commented and edited by T7166PA--Starts
//					else if (!iTSuperAdminFlag && region.equalsIgnoreCase("A")) {
//						criteria.add(Restrictions.eq("userRegion", region));
//					}
					else if (!(iTSuperAdminFlag || globalAdminFlag) && (region.equalsIgnoreCase("A")||region.equalsIgnoreCase("C")||
							region.equalsIgnoreCase("I"))) {
						criteria.add(Restrictions.eq("userRegion", region));
					}
					//Commented and edited by T7166PA--Ends
				}
		}
		//Added by T9371M0 for emailAll starts
//		criteria.setProjection(Projections.projectionList()
//	      .add(Projections.property("email")));
		
//		  ProjectionList proList = Projections.projectionList();
//		  proList.add(Projections.property("email"));
//		  criteria.setProjection(proList);
//		  criteria.setProjection(Projections.property("email"));

		List usersMailList = criteria.list();
		//Added by T9371M0 for emailAll ends
		logger.info("Exit - UserDAO findUserEmailAll");
		return usersMailList;

	}
	/**
	 * @param session
	 * @param config
	 * @param supervisorId
	 * @param region
	 * @param iTSuperAdminFlag
	 * @param start
	 * @param end
	 * Returns user results for selected search criteria
	 * @return
	 * @throws BaseException
	 */
	
	//SeRV01016061 - Method modified for Cookies issue - T8860SG 
	public PageResult<User> findUserActivation(Session session, PageConfig config,
			String region, boolean iTSuperAdminFlag ,Date start, Date end) throws BaseException {
		List apacRegs=new ArrayList<String>();
		apacRegs.add("I");apacRegs.add("C");apacRegs.add("A");
		logger.info("Enter - UserDAO findUserActivation");
		Criteria criteria = session.createCriteria(User.class);

		if (config.getSearchField().equals(UserData.SUPERVISOR)
				|| config.getSearchField().equals(UserData.BACKUP_APPROVAL)) {
			criteria = addSearchCriteria(criteria, UserData.TID, config
					.getSearchValue());
			// added for SeRV00649233 start
			criteria.addOrder(Order.asc("id"));
			// added for SeRV00649233 end
			List<User> users = criteria.list();
			if (users.size() < 1) {
				throw new BaseException("Can not find a user for this TID");
			}
			User su = users.get(users.size() - 1);
			criteria = session.createCriteria(User.class);
			if (config.getSearchField().equals(UserData.SUPERVISOR)) {
				criteria.add(Restrictions.eq("supervisor", su));
			}
			if (config.getSearchField().equals(UserData.BACKUP_APPROVAL)) {
				criteria.add(Restrictions.eq("backupApproval", su));
			}

			criteria.setProjection( Projections.projectionList()
			        .add( Projections.max("id") )
			        .add( Projections.groupProperty(UserData.TID) ));
			criteria.add(Restrictions.le("createdDate", end));
		} else {
			if (config.getSearchField() != null
					&& config.getSearchValue() != null) {
				criteria = addSearchCriteria(criteria, config.getSearchField(),
						config.getSearchValue());
			}

			criteria.setProjection( Projections.projectionList()
			        .add( Projections.max("id") )
			        .add( Projections.groupProperty(UserData.TID) ));
			criteria.add(Restrictions.le("createdDate", end));
			// Add region criteria
			
			//Change 1 - T9434gb - for India-China Region Handling
				if(!(iTSuperAdminFlag ) && (region.equalsIgnoreCase("N")||region.equalsIgnoreCase("I") || region.equalsIgnoreCase("C")) ){
					criteria.add(Restrictions.eq("userRegion", region));
				}else if (!(iTSuperAdminFlag ) && region.equalsIgnoreCase("A")) {
					criteria.add(Restrictions.in("userRegion", apacRegs));
				}
				//Change 1 - T9434gb - for India-China Region Handling
		}
		//added by t5317sk
		if(config.getSearchField().equals(UserData.JOBCODETITLE)){
			criteria = addSearchCriteria(criteria, UserData.JOBCODETITLE, config.getSearchValue());
		}
		
		else if(config.getSearchField().equals(UserData.CID)){
			criteria = addSearchCriteria(criteria, UserData.CID, config.getSearchValue());
		}
		//end by t5317sk
		List results = criteria.list();
		List idList=new ArrayList();
		Iterator resultIterator = results.iterator();

		while(resultIterator.hasNext())
		{
			Object[] obj = (Object[]) resultIterator.next();
			idList.add(obj[0]);
		}
		
		int totalSize = 0;
		List<User> users = null;
		if(idList.size()>0)
		{
		criteria = null;
		criteria = session.createCriteria(User.class);
		criteria.add(Restrictions.in("id", idList));
//		usersMailList = criteria.list();
		//Added by T9371M0 for emailAll ends
//		ScrollableResults scr = criteria.scroll();
//		scr.last();
//		totalSize = scr.getRowNumber() + 1;

		if (config.getSortField() != null) {
			if (config.isAscending()) {
				if (config.getSearchValue() != null
						&& !"".equals(config.getSearchValue().trim())
						&& config.getSearchField().equals(UserData.FIRST_NAME)
						&& config.getSortField().equals(UserData.FIRST_NAME)) {
					criteria.addOrder(
							Order.asc(UserData.FIRST_NAME).ignoreCase())
							.addOrder(
									Order.asc(UserData.LAST_NAME).ignoreCase());
				} else if (config.getSearchValue() != null
						&& !"".equals(config.getSearchValue().trim())
						&& config.getSearchField().equals(UserData.LAST_NAME)
						&& config.getSortField().equals(UserData.LAST_NAME)) {
					criteria
							.addOrder(
									Order.asc(UserData.LAST_NAME).ignoreCase())
							.addOrder(
									Order.asc(UserData.FIRST_NAME).ignoreCase());
				} else {
					criteria.addOrder(Order.asc(config.getSortField())
							.ignoreCase());
				}
			} else {
				if (config.getSortField().equals(UserData.FIRST_NAME)) {
					criteria
							.addOrder(
									Order.desc(UserData.FIRST_NAME)
											.ignoreCase())
							.addOrder(
									Order.desc(UserData.LAST_NAME).ignoreCase());
				} else if (config.getSortField().equals(UserData.LAST_NAME)) {
					criteria.addOrder(
							Order.desc(UserData.LAST_NAME).ignoreCase())
							.addOrder(
									Order.desc(UserData.FIRST_NAME)
											.ignoreCase());
				} else {
					criteria.addOrder(Order.desc(config.getSortField())
							.ignoreCase());
				}
			}
		}

			
			criteria.setFirstResult(config.getOffset());
			criteria.setMaxResults(config.getLimit());
			// RP-APAC
			
			//Change 1 - T9434gb - for India-China Region Handling
			if(!(iTSuperAdminFlag ) && (region.equalsIgnoreCase("N")||region.equalsIgnoreCase("I") || region.equalsIgnoreCase("C")) ){
				criteria.add(Restrictions.eq("userRegion", region));
			}else if (!(iTSuperAdminFlag ) && region.equalsIgnoreCase("A")) {
				criteria.add(Restrictions.in("userRegion", apacRegs));
			}
			//Change 1 - T9434gb - for India-China Region Handling
			
			//Commented and edited by T7166PA--Ends
			users = criteria.list();
			
	}	
			logger.info("Exit - UserDAO findUserActivation");
		return new PageResult<User>(users, totalSize);
	}
	
	// new method for SeRV00299944

	public double getBaseHours(Session session, String tid, Date startWeek) {

		Transaction tx = session.beginTransaction();
		Criteria criteria = session.createCriteria(BaseHours.class);
		double baseHourValue = 0.0;
		criteria.add(Restrictions.eq("userTid", tid.toUpperCase()));
		criteria.add(Restrictions.le("weekStart", startWeek));
		criteria.addOrder(Order.asc("weekStart"));
		List<BaseHours> baseHourInfo = criteria.list();
		for (int i = 0; i < baseHourInfo.size(); i++) {
			baseHourValue = baseHourInfo.get(i).getBaseHours();
		}

		return baseHourValue;
	}

	// end SeRV00299944

	// new method for comment SeRV

	public ArrayList getUserComments(Session session, String tid, Date startWeek) {

		ArrayList commentWithStat = new ArrayList();
		Criteria criteria = session.createCriteria(UserComments.class);
		String userComment = "";
		String commentStatus = "";
		UserCommentsCk ck = new UserCommentsCk();
		ck.setUserTid(tid.toUpperCase());
		ck.setWeekStart(startWeek);

		criteria.add(Restrictions.eq("commentCk", ck));

		List<UserComments> userComments = criteria.list();
		for (int i = 0; i < userComments.size(); i++) {
			userComment = userComments.get(i).getUserComments();
			commentStatus = userComments.get(i).getStatus();
		}
		commentWithStat.add(userComment);
		commentWithStat.add(commentStatus);

		return commentWithStat;
	}

	// end comment SeRV

	public List<User> findAll(Session session) {
		Criteria criteria = session.createCriteria(User.class);
		return (List<User>) criteria.list();
	}

	public List<User> find(Session session, String searchField,
			String searchValue) {
		Criteria criteria = session.createCriteria(User.class);
		addSearchCriteria(criteria, searchField, searchValue);
		return (List<User>) criteria.list();
	}

	private Criteria addSearchCriteria(Criteria criteria, String searchField,
			String searchValue) {

		// changed for SeRV00562464
		if (searchField.equalsIgnoreCase(UserData.LOCATION_NUMBER)) {

			List ilds = getILDsAgainstLocNum(searchValue);
			if (ilds.size() == 0) {
				criteria.add(Restrictions.isNull("locDeptName"));
			} else {
				criteria.add(Restrictions.in("locDeptName", ilds));
			}

		} else if (searchField.equalsIgnoreCase(UserData.DEPARTMENT_NUMBER)) {

			List ilds = getILDsAgainstDeptNum(searchValue);
			if (ilds.size() == 0) {
				criteria.add(Restrictions.isNull("locDeptName"));
			} else {
				criteria.add(Restrictions.in("locDeptName", ilds));
			}

		} else if (searchField.equalsIgnoreCase(UserData.DEPARTMENT_NAME)) {

			List ilds = getILDsAgainstDeptName(searchValue);
			if (ilds.size() == 0) {
				criteria.add(Restrictions.isNull("locDeptName"));
			} else {
				criteria.add(Restrictions.in("locDeptName", ilds));
			}

		} else {
			// end SeRV00562464
			// criteria.add(Restrictions.ilike(searchField, searchValue,
			// MatchMode.ANYWHERE));//commented for SeRV00649233
			criteria.add(Restrictions.ilike(searchField, searchValue,
					MatchMode.START));// added for SeRV00649233

		}
		// criteria.addOrder(Order.asc("id"));//commented for SeRV00649233

		return criteria;
	}

	// added for SeRV00462464
	/**
	 * This method is for getting the ILDs against location number
	 * 
	 * @param searchValue
	 *            - Location number
	 * 
	 * @return ILD List
	 */
	@SuppressWarnings("finally")
	public List getILDsAgainstLocNum(String LocationNumber) {
		Session session = null;
		List<LDAPLocDeptName> list = new ArrayList<LDAPLocDeptName>();
		try {
			session = DAOManager.getSession();
			Criteria criteria = session.createCriteria(LDAPLocDeptName.class);
			criteria.add(Restrictions.eq("locationNumber", LocationNumber));
			list = criteria.list();
		} finally {
			if (session != null) {
				session.close();
			}
			return list;
		}

	}

	/**
	 * This method is for getting the ILDs against department number
	 * 
	 * @param departmentNumber
	 *            - Department number
	 * 
	 * @return ILD List
	 */
	@SuppressWarnings("finally")
	public List getILDsAgainstDeptNum(String departmentNumber) {
		Session session = null;
		List<LDAPLocDeptName> list = new ArrayList<LDAPLocDeptName>();
		try {
			session = DAOManager.getSession();
			Criteria criteria = session.createCriteria(LDAPLocDeptName.class);
			criteria.add(Restrictions.eq("departmentNumber", departmentNumber));
			list = criteria.list();
		} finally {
			if (session != null) {
				session.close();
			}
			return list;
		}

	}

	/**
	 * This method is for getting the ILDs against department name
	 * 
	 * @param departmentName
	 *            - Department Name
	 * 
	 * @return ILD List
	 */
	@SuppressWarnings("finally")
	public List getILDsAgainstDeptName(String departmentName) {
		Session session = null;
		List<LDAPLocDeptName> list = new ArrayList<LDAPLocDeptName>();
		try {
			session = DAOManager.getSession();
			Criteria criteria = session.createCriteria(LDAPLocDeptName.class);
			// criteria.add(Restrictions.ilike("departmentName",
			// departmentName,MatchMode.ANYWHERE));//commented for SeRV00649233
			criteria.add(Restrictions.ilike("departmentName", departmentName,
					MatchMode.START));// added for SeRV00649233
			list = criteria.list();
		} finally {
			if (session != null) {
				session.close();
			}
			return list;
		}

	}

	// end SeRV00462464
	// SeRV00465408 - t2763co start
	/*
	 * This method queries the db to get users for various filter criteria
	 * 
	 * @param config paging required
	 * 
	 * @param session session in which user is
	 * 
	 * @param supervisorId
	 * 
	 * @param start Start of the week
	 * 
	 * @param end End of the week
	 * 
	 * @param isSu To check is the reports should be under supervisor
	 * 
	 * @return PageResult<User> getUser The users obtained from the database
	 * 
	 * @throws BaseException
	 */
	public PageResult<User> getUsers(PagingLoadConfig config, Session session,
			long supervisorId, Date start, Date end, boolean isSu,
			String status, String empType) // SeRV00679285 - T8860SG - Added
											// empType
			throws BaseException {
		logger.info("Enter method getUsers");
		User supervisor = (User) session.load(User.class, supervisorId);
		if (supervisor == null) {
			throw new BaseException("Can not find a Supervisor");
		}
		Query sql = null;

		String condition = null;
		// SeRV00679285 - T8860SG - Starts
		StringBuilder sqlString = new StringBuilder(USER_QUERY);
		// SeRV00679285 - T8860SG - Ends

		if (isSu) {
			condition = SUP_CONDITION;
		} else {
			condition = BKUP_CONDITION;
		}

		// SeRV00679285 - T8860SG - Starts
		if (status == null) {
			sqlString.append(condition).append(WHERE_CLAUSE);
		} else {
			sqlString.append(condition).append(WHERE_CLAUSE).append(
					STATUS_CONDITION);
		}

		if (empType == null) {
			sqlString.append(GROUPBY_CONDITION);
		} else {
			sqlString.append(EMPTYPE_CONDITION).append(GROUPBY_CONDITION);
		}

		sql = session.createSQLQuery(sqlString.toString());
		// SeRV00679285 - T8860SG - Ends

		sql.setDate("start", start);
		sql.setDate("end", end);
		sql.setLong("supervisorId", supervisor.getId());
		if (status != null) {
			sql.setString("status", status);
		}

		// SeRV00679285 - T8860SG - Starts
		if (empType != null) {
			sql.setString("emptype", empType);
		}
		// SeRV00679285 - T8860SG - Ends

		ScrollableResults scr = sql.scroll();
		scr.last();
		int totalSize = scr.getRowNumber() + 1;

		sql.setFirstResult(config.getOffset());
		sql.setMaxResults(config.getLimit());

		// criteria.addOrder(Order.asc("id"));
		List<User> users = new ArrayList<User>();
		List usersList = new ArrayList();
		usersList = sql.list();
		int size = usersList.size();
		User userObj = null;

		String prevTid = "0";
		for (int i = 0; i < size; i++) {
			Object[] objArray = (Object[]) usersList.get(i);
			// Defect fix for Quick Approval Tab - t2763co start
			// if (!prevTid.equalsIgnoreCase(objArray[1].toString().trim())) {
			// Defect fix for Quick Approval Tab - t2763co end
				userObj = new User();
				userObj.setTid(objArray[1].toString().trim());
				prevTid = objArray[1].toString().trim();
				userObj.setId(((Integer) objArray[2]).longValue());
				userObj.setFirstName(objArray[3].toString().trim());
				userObj.setLastName(objArray[4].toString().trim());
				userObj.setDeactivationDate((Date) objArray[5]);
				userObj.setRemovalDate((Date) objArray[6]);
				userObj.setCreatedDate((Date) objArray[7]);

				if (isSu) {
					userObj.setSupervisor(supervisor);

					if (objArray[9] != null) {
						User bkupData = (User) session.load(User.class,
								((Integer) objArray[9]).longValue());
						userObj.setBackupApproval(bkupData);
					}
				} else {

					if (objArray[8] != null) {
						User supervisorData = (User) session.load(User.class,
								((Integer) objArray[8]).longValue());
						userObj.setSupervisor(supervisorData);
					}
					userObj.setBackupApproval(supervisor);

				}
				long locDeptNo = (Integer) objArray[10];
				LDAPLocDeptName locDept = (LDAPLocDeptName) session.load(
						LDAPLocDeptName.class, locDeptNo);
				userObj.setLocDeptName(locDept);
				userObj.setEmail(objArray[11].toString().trim());
				userObj.setEmployeeType(objArray[12].toString().trim());
				userObj.setPhoneNumber(objArray[13].toString().trim());
				if (objArray[14] != null) {
					userObj.setHourStatus(objArray[14].toString().trim());
				}
				users.add(userObj);
			}
		// Defect fix for Quick Approval Tab - t2763co start
		// }
		// Defect fix for Quick Approval Tab - t2763co end
		logger.info("Exit method getUsers");
		return new PageResult<User>(users, totalSize);
	}

	// SeRV00465408 - t2763co end

	// SeRV00679285 - T8860SG - Starts
	/**
	 * This method returns Distinct list of Employee Types under a Supervisor
	 * @param userId
	 * @param start
	 * @param end
	 * @param isSupervisor
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserData> getEmpTypesForSupervisor(Long userId, Date start,
			Date end, boolean isSupervisor) {
		Session session = null;
		List<User> listUser = null;
		List<UserData> listUserData = null;
		List usersList = new ArrayList();
		Query sqlQuery = null;
		try {
			session = DAOManager.getSession();
			session.load(User.class, userId);
			StringBuilder sqlString = new StringBuilder(EMPTYPE_DROPDOWN_QUERY);
			if (isSupervisor) {
				sqlString.append(SUP_CONDITION);
			} else {
				sqlString.append(BKUP_CONDITION);
			}
			sqlString.append(WHERE_CLAUSE_EMPTYPE);

			sqlQuery = session.createSQLQuery(sqlString.toString());
			if (userId != null) {
				sqlQuery.setLong("supervisorId", userId);
			}
			// sqlQuery.setDate("start", start);
			sqlQuery.setDate("end", end);
			usersList = sqlQuery.list();
			// for(int i=0; i<usersList.size();i++)
			// {
			// UserData userDataObj = new UserData();
			// userDataObj.setEmployeeType(usersList.get(i));
			// listUserData.add(userDataObj);
			// }
			if (usersList != null) {
				listUser = new ArrayList<User>();
				listUserData = new ArrayList<UserData>();
				int size = usersList.size();
				String prevTid = "0";

				for (int i = 0; i < size; i++) {
					Object[] objArray = (Object[]) usersList.get(i);
					if (!prevTid
							.equalsIgnoreCase(objArray[0].toString().trim())) {
						User userDataObj = new User();
						userDataObj.setTid(objArray[0].toString().trim());
						prevTid = objArray[0].toString().trim();
						userDataObj.setDeactivationDate((Date) objArray[1]);
						userDataObj.setRemovalDate((Date) objArray[2]);
						userDataObj.setCreatedDate((Date) objArray[3]);
						userDataObj.setEmployeeType(objArray[4].toString()
								.trim());

						listUser.add(userDataObj);
					}
				}
				// ArrayList<User> newUsers = DTOManager.getNewUsers(listUser,
				// start, end);
				// Logic to eliminate Duplicate entries for EmpType
				List<String> tempString = new ArrayList<String>();
				String tempEmpType = "";

				UserData userDataTemp = new UserData();
				userDataTemp.setEmployeeType("All");
				listUserData.add(userDataTemp);

				for (User newUserObj : listUser) {
					tempEmpType = newUserObj.getEmployeeType();
					if (!tempString.contains(tempEmpType)) {
						UserData userData = new UserData();
						userData.setEmployeeType(tempEmpType);
						listUserData.add(userData);
						tempString.add(tempEmpType);
					}
				}
			} else {
				//If no users are Present under a Supervisor
				listUserData = new ArrayList<UserData>();
				UserData userDataTemp = new UserData();
				userDataTemp.setEmployeeType(UIConstants.ALL);
				listUserData.add(userDataTemp);
			}
		} catch (Exception e) {
			//logger.error("Error: " + e.getStackTrace());
			//e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return listUserData;
	}
	// SeRV00679285 - T8860SG - Ends
}
