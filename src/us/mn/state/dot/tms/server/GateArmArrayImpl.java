/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2019  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.ActionPlanJob.SCHED_LOG;
import static us.mn.state.dot.tms.server.GateArmSystem.checkEnabled;
import static us.mn.state.dot.tms.server.GateArmSystem.sendEmailAlert;
import static us.mn.state.dot.tms.server.MainServer.TIMER;

/**
 * A Gate Arm array is a group of gate arms at a single ramp location.
 * All gate arms in an array are always controlled as a group.
 *
 * @author Douglas Lau
 */
public class GateArmArrayImpl extends DeviceImpl implements GateArmArray {

	/** Exception thrown for interlock conflicts */
	static private final ChangeVetoException INTERLOCK_CONFLICT =
		new ChangeVetoException("INTERLOCK CONFLICT");

	/** Load all the gate arm arrays */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GateArmArrayImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"prereq, camera, approach, action_plan, open_phase, " +
			"closed_phase FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmArrayImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("prereq", prereq);
		map.put("camera", camera);
		map.put("approach", approach);
		map.put("action_plan", action_plan);
		map.put("open_phase", open_phase);
		map.put("closed_phase", closed_phase);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new gate arm array with a string name */
	public GateArmArrayImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		GateArmSystem.disable(n, "create array");
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(ResultSet row) throws SQLException {
		this(row.getString(1),    // name
		     row.getString(2),    // geo_loc
		     row.getString(3),    // controller
		     row.getInt(4),       // pin
		     row.getString(5),    // notes
		     row.getString(6),    // prereq
		     row.getString(7),    // camera
		     row.getString(8),    // approach
		     row.getString(9),    // action_plan
		     row.getString(10),   // open_phase
		     row.getString(11));  // closed_phase
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(String n, String loc, String c, int p,
		String nt, String pr, String cam, String ap, String pln,
		String op, String cp)
	{
		this(n, lookupGeoLoc(loc), lookupController(c), p, nt, pr,
		     lookupCamera(cam), lookupCamera(ap), lookupActionPlan(pln),
		     lookupPlanPhase(op), lookupPlanPhase(cp));
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, String pr, Camera cam, Camera ap,
		ActionPlanImpl pln, PlanPhaseImpl op, PlanPhaseImpl cp)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		prereq = pr;
		camera = cam;
		approach = ap;
		action_plan = pln;
		open_phase = op;
		closed_phase = cp;
		initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
		GateArmSystem.disable(name, "destroy array");
	}

	/** Set the controller of the device */
	@Override
	public void doSetController(Controller c) throws TMSException{
		throw new ChangeVetoException("Cannot assign controller");
	}

	/** Set the controller I/O pin number */
	@Override
	public void doSetPin(int p) throws TMSException {
		throw new ChangeVetoException("Cannot assign pin");
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Prerequisite gate arm array */
	private String prereq;

	/** Set the prerequisite gate arm array */
	@Override
	public void setPrereq(String pr) {
		GateArmSystem.disable(name, "prereq");
		prereq = pr;
	}

	/** Set the prerequisite gate arm array */
	public void doSetPrereq(String pr) throws TMSException {
		if (!objectEquals(pr, prereq)) {
			store.update(this, "prereq", pr);
			setPrereq(pr);
		}
	}

	/** Get prerequisite gate arm array */
	@Override
	public String getPrereq() {
		return prereq;
	}

	/** Get prerequisite gate arm array */
	private GateArmArrayImpl getPrerequisite() {
		return (GateArmArrayImpl) GateArmArrayHelper.lookup(prereq);
	}

	/** Camera from which this can be seen */
	private Camera camera;

	/** Set the verification camera */
	@Override
	public void setCamera(Camera c) {
		GateArmSystem.disable(name, "camera");
		camera = c;
	}

	/** Set the verification camera */
	public void doSetCamera(Camera c) throws TMSException {
		if (c != camera) {
			store.update(this, "camera", c);
			setCamera(c);
		}
	}

	/** Get verification camera */
	@Override
	public Camera getCamera() {
		return camera;
	}

	/** Camera to view approach */
	private Camera approach;

	/** Set the approach camera */
	@Override
	public void setApproach(Camera c) {
		GateArmSystem.disable(name, "approach");
		approach = c;
	}

	/** Set the approach camera */
	public void doSetApproach(Camera c) throws TMSException {
		if (c != approach) {
			store.update(this, "approach", c);
			setApproach(c);
		}
	}

	/** Get approach camera */
	@Override
	public Camera getApproach() {
		return approach;
	}

	/** Action plan */
	private ActionPlanImpl action_plan;

	/** Set the action plan */
	@Override
	public void setActionPlan(ActionPlan ap) {
		GateArmSystem.disable(name, "actionPlan");
		if (ap instanceof ActionPlanImpl)
			action_plan = (ActionPlanImpl) ap;
	}

	/** Set the action plan */
	public void doSetActionPlan(ActionPlan ap) throws TMSException {
		if (ap != action_plan) {
			store.update(this, "action_plan", ap);
			setActionPlan(ap);
		}
	}

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Action plan phase for OPEN state */
	private PlanPhaseImpl open_phase;

	/** Set the action plan phase for OPEN state */
	@Override
	public void setOpenPhase(PlanPhase p) {
		GateArmSystem.disable(name, "openPhase");
		if (p instanceof PlanPhaseImpl)
			open_phase = (PlanPhaseImpl) p;
	}

	/** Set the action plan phase for OPEN state */
	public void doSetOpenPhase(PlanPhase p) throws TMSException {
		if (p != open_phase) {
			store.update(this, "open_phase", p);
			setOpenPhase(p);
		}
	}

	/** Get the action plan phase for OPEN state */
	@Override
	public PlanPhase getOpenPhase() {
		return open_phase;
	}

	/** Action plan phase for CLOSED state */
	private PlanPhaseImpl closed_phase;

	/** Set the action plan phase for CLOSED state */
	@Override
	public void setClosedPhase(PlanPhase p) {
		GateArmSystem.disable(name, "closedPhase");
		if (p instanceof PlanPhaseImpl)
			closed_phase = (PlanPhaseImpl) p;
	}

	/** Set the action plan phase for CLOSED state */
	public void doSetClosedPhase(PlanPhase p) throws TMSException {
		if (p != closed_phase) {
			store.update(this, "closed_phase", p);
			setClosedPhase(p);
		}
	}

	/** Get the action plan phase for CLOSED state */
	@Override
	public PlanPhase getClosedPhase() {
		return closed_phase;
	}

	/** Array of all gate arms */
	private transient final GateArmImpl[] arms = new GateArmImpl[MAX_ARMS];

	/** Get one gate arm */
	private GateArmImpl getArm(int i) {
		GateArmImpl[] a = arms;
		return (a != null) ? a[i] : null;
	}

	/** Get array of gate arms */
	public synchronized GateArmImpl[] getArms() {
		return Arrays.copyOf(arms, MAX_ARMS);
	}

	/** Set one gate arm.
	 * @param idx Array index.
	 * @param ga Gate Arm */
	public synchronized void setIndex(int idx, GateArmImpl ga)
		throws TMSException
	{
		idx--;
		if (idx < 0 || idx >= MAX_ARMS)
			throw new ChangeVetoException("Invalid index");
		if (ga != null && arms[idx] != null)
			throw new ChangeVetoException("Already assigned");
		arms[idx] = ga;
	}

	/** The owner of the next state to be requested.  This is a write-only
	 * SONAR attribute. */
	private transient User ownerNext;

	/** Set the next state owner.  When a user sends a new state to the
	 * gate arm array, two attributes must be set: ownerNext and armState.
	 * There can be a race between two clients setting these attributes.  If
	 * ownerNext is non-null when being set, then a race has been detected,
	 * meaning two clients are trying to set the state at the same time. */
	@Override
	public synchronized void setOwnerNext(User o) {
		if (ownerNext != null && o != null) {
			logError("OWNER CONFLICT: " + ownerNext.getName() +
			         " vs. " + o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** Gate arm state */
	private transient GateArmState arm_state = GateArmState.UNKNOWN;

	/** Set the next arm state (request change) */
	@Override
	public void setArmStateNext(int gas) {
		// Do nothing; required by iface
	}

	/** Set the arm state (request change) */
	public synchronized void doSetArmStateNext(int gas) throws TMSException{
		try {
			if (ownerNext != null)
				doSetArmStateNext(gas, ownerNext);
			else
				throw new ChangeVetoException("OWNER CONFLICT");
		}
		finally {
			// ownerNext is only valid for one message, clear it
			ownerNext = null;
		}
	}

	/** Set the arm state (request change) */
	private void doSetArmStateNext(int gas, User o) throws TMSException {
		final GateArmState cs = arm_state;
		GateArmState rs = validateStateReq(
			GateArmState.fromOrdinal(gas), cs);
		if ((rs != cs) && checkEnabled())
			requestArmState(rs, ownerNext);
	}

	/** Validate a new requested gate arm state.
	 * @param rs Requested gate arm state.
	 * @param cs Current arm state.
	 * @return Validated state: OPENING, WARN_CLOSE or CLOSING.
	 * @throws TMSException for invalid state change or interlock. */
	private GateArmState validateStateReq(GateArmState rs, GateArmState cs)
		throws TMSException
	{
		if (rs == GateArmState.OPENING) {
			if (lock_state.isOpenDenied())
				throw INTERLOCK_CONFLICT;
			if (cs == GateArmState.CLOSED ||
			   cs == GateArmState.WARN_CLOSE)
				return rs;
		}
		if (rs == GateArmState.WARN_CLOSE) {
			if (lock_state.isCloseDenied())
				throw INTERLOCK_CONFLICT;
			if (cs == GateArmState.OPEN)
				return rs;
		}
		if (rs == GateArmState.CLOSING) {
			if (lock_state.isCloseDenied())
				throw INTERLOCK_CONFLICT;
			if (cs == GateArmState.WARN_CLOSE ||
			   cs == GateArmState.FAULT)
				return rs;
		}
		throw new ChangeVetoException("INVALID STATE CHANGE: " + cs +
			" to " + rs);
	}

	/** Request a change to the gate arm state for all arms in array.
	 * @param rs Requested gate arm state.
	 * @param o User requesting new state. */
	private synchronized void requestArmState(GateArmState rs, User o)
		throws TMSException
	{
		if (rs == GateArmState.WARN_CLOSE) {
			setArmState(rs);
			updatePlanPhase();
			return;
		}
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null)
				ga.requestArmState(rs, o);
		}
	}

	/** Set the arm state */
	private void setArmState(GateArmState gas) {
		arm_state = gas;
		notifyAttribute("armState");
		updateStyles();
		if (gas == GateArmState.TIMEOUT)
			sendEmailAlert("COMMUNICATION FAILED: " + name);
		if (gas == GateArmState.FAULT)
			sendEmailAlert("FAULT: " + name);
	}

	/** Update the action plan phase */
	private void updatePlanPhase() {
		ActionPlanImpl ap = action_plan;
		if (ap != null) {
			try {
				updatePlanPhase(ap);
			}
			catch (TMSException e) {
				logError("updatePlanPhase: " + e.getMessage());
			}
		}
	}

	/** Update the action plan phase */
	private void updatePlanPhase(ActionPlanImpl ap) throws TMSException {
		if (isMsgOpen()) {
			PlanPhase op = open_phase;
			if (op != null && ap.setPhaseNotify(op))
				updateDmsActions();
		} else {
			PlanPhase cp = closed_phase;
			if (cp != null && ap.setPhaseNotify(cp))
				updateDmsActions();
		}
	}

	/** Test if message should be open */
	private boolean isMsgOpen() {
		return isActive() && arm_state == GateArmState.OPEN;
	}

	/** Update scheduled DMS action */
	private void updateDmsActions() {
		TIMER.addJob(new DmsActionJob(SCHED_LOG));
	}

	/** Update the arm state */
	public void updateArmState() {
		GateArmState cs = arm_state;
		GateArmState gas = aggregateArmState();
		// Don't update WARN_CLOSE back to OPEN
		if (gas != cs &&
		   (gas != GateArmState.OPEN || cs != GateArmState.WARN_CLOSE))
			setArmState(gas);
		else
			checkEnabled();
		updatePlanPhase();
	}

	/** Get the aggregate arm state for all arms in the array */
	private GateArmState aggregateArmState() {
		boolean unknown = false;
		boolean fault = false;
		boolean opening = false;
		boolean open = false;
		boolean closing = false;
		boolean closed = false;
		boolean timeout = false;
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null && ga.isActive()) {
				GateArmState gas = ga.getArmStateEnum();
				switch (gas) {
				case UNKNOWN:
					unknown = true;
					break;
				case FAULT:
					fault = true;
					break;
				case OPENING:
					opening = true;
					break;
				case OPEN:
					open = true;
					break;
				case CLOSING:
					closing = true;
					break;
				case CLOSED:
					closed = true;
					break;
				case TIMEOUT:
					timeout = true;
					break;
				}
			}
		}
		if (unknown)
			return GateArmState.UNKNOWN;
		if (timeout)
			return GateArmState.TIMEOUT;
		if (fault)
			return GateArmState.FAULT;
		if (opening && !closing)
			return GateArmState.OPENING;
		if (closing && !opening)
			return GateArmState.CLOSING;
		if (open && !(closed || opening || closing))
			return GateArmState.OPEN;
		if (closed && !(open || opening || closing))
			return GateArmState.CLOSED;
		return GateArmState.FAULT;
	}

	/** Get the arm state */
	@Override
	public int getArmState() {
		return arm_state.ordinal();
	}

	/** Calculate item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (isClosed())
			s |= ItemStyle.CLOSED.bit();
		if (isOpen())
			s |= ItemStyle.OPEN.bit();
		if (isMoving())
			s |= ItemStyle.MOVING.bit();
		return s;
	}

	/** Update the item styles */
	@Override
	public void updateStyles() {
		super.updateStyles();
		GateArmSystem.checkInterlocks(getRoad());
		GateArmSystem.updateDependants();
		setSystemEnable(checkEnabled());
		setOpenConflict(lock_state.isOpenDenied() &&
			(isOpen() || isTimeout()));
		setCloseConflict(lock_state.isCloseDenied() && isClosed());
	}

	/** Lock state */
	private transient GateArmLockState lock_state = new GateArmLockState();

	/** Set the interlock flag */
	private void setInterlockNotify() {
		notifyAttribute("interlock");
		sendInterlocks();
	}

	/** Send gate arm interlock settings */
	private void sendInterlocks() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null)
				ga.sendInterlocks();
		}
	}

	/** Get the interlock enum */
	@Override
	public int getInterlock() {
		return lock_state.getInterlock().ordinal();
	}

	/** Check if arm open interlock in effect. */
	public boolean isOpenInterlock() {
		return lock_state.isOpenInterlock();
	}

	/** Check open/close state of prerequisite gate arm array */
	private void checkPrerequisite() {
		GateArmArrayImpl pr = getPrerequisite();
		setPrereqClosed(pr != null && !pr.isFullyOpen());
	}

	/** Set flag to indicate prerequisite closed */
	private void setPrereqClosed(boolean c) {
		if (lock_state.setPrereqClosed(c))
			setInterlockNotify();
	}

	/** Dependant open flag */
	private transient boolean dep_open = false;

	/** Clear dependant open flag */
	public void clearDependant() {
		dep_open = false;
	}

	/** Check open/close state of dependant gate arm array */
	public void checkDependant() {
		GateArmArrayImpl pr = getPrerequisite();
		if (pr != null)
			pr.dep_open = (pr.dep_open || isPossiblyOpen());
	}

	/** Set dependant open state */
	public void setDependant() {
		setDependantOpen(dep_open);
		checkPrerequisite();
	}

	/** Set flag to indicate dependant gate arm open */
	private void setDependantOpen(boolean o) {
		if (lock_state.setDependantOpen(o))
			setInterlockNotify();
	}

	/** Set flag to enable gate arm system */
	public void setSystemEnable(boolean e) {
		if (lock_state.setSystemEnable(e && isActive()))
			setInterlockNotify();
	}

	/** Open conflict detected flag.  This is initially set to true because
	 * devices start in failed state after a server restart. */
	private transient boolean open_conflict = true;

	/** Set open conflict state */
	private void setOpenConflict(boolean c) {
		if (c != open_conflict) {
			open_conflict = c;
			if (c)
				sendEmailAlert("OPEN CONFLICT: " + name);
		}
	}

	/** Close conflict detected flag. */
	private transient boolean close_conflict = false;

	/** Set close conflict state */
	private void setCloseConflict(boolean c) {
		if (c != close_conflict) {
			close_conflict = c;
			if (c)
				sendEmailAlert("CLOSE CONFLICT: " + name);
		}
	}

	/** Get gate arm road */
	public Road getRoad() {
		GeoLoc gl = getGeoLoc();
		return (gl != null) ? gl.getRoadway() : null;
	}

	/** Get gate arm road direction.
	 * @return Index of road direction, or 0 for unknown */
	public int getRoadDir() {
		GeoLoc gl = getGeoLoc();
		return (gl != null) ? gl.getRoadDir() : 0;
	}

	/** Set the valid open direction for road.
	 * @param d Valid open direction; 0 for any, -1 for none */
	public void setOpenDirection(int d) {
		int gd = getRoadDir();
		setOpposingOpen(d != 0 && d != gd);
	}

	/** Set flag to indicate opposing direction open */
	private void setOpposingOpen(boolean o) {
		if (lock_state.setOpposingOpen(o))
			setInterlockNotify();
	}

	/** Get the active status */
	@Override
	public boolean isActive() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null && ga.isActive())
				return true;
		}
		return false;
	}

	/** Get the failure status */
	@Override
	public boolean isFailed() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null && ga.isActive() && ga.isFailed())
				return true;
		}
		return false;
	}

	/** Test if gate arm is closed */
	private boolean isClosed() {
		return isOnline() && arm_state == GateArmState.CLOSED;
	}

	/** Test if gate arm is possibly open */
	public boolean isPossiblyOpen() {
		return isActive() &&
		       arm_state != GateArmState.CLOSED &&
		       arm_state != GateArmState.UNKNOWN;
	}

	/** Test if gate arm is open */
	private boolean isOpen() {
		return isOnline() && isPossiblyOpen();
	}

	/** Test if gate arm is in TIMEOUT state */
	private boolean isTimeout() {
		return isActive() && arm_state == GateArmState.TIMEOUT;
	}

	/** Test if gate arm is fully open */
	public boolean isFullyOpen() {
		return isOnline() && arm_state == GateArmState.OPEN;
	}

	/** Test if gate arm is moving */
	private boolean isMoving() {
		return isOnline() &&
		      (arm_state == GateArmState.OPENING ||
		       arm_state == GateArmState.CLOSING);
	}

	/** Test if gate arm needs maintenance */
	@Override
	protected boolean needsMaintenance() {
		return arm_state == GateArmState.FAULT;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null)
				ga.sendDeviceRequest(dr);
		}
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll() {
		// handled by individual gate arms
	}
}
