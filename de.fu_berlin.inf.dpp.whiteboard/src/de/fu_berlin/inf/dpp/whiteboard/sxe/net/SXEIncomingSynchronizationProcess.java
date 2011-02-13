package de.fu_berlin.inf.dpp.whiteboard.sxe.net;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.whiteboard.sxe.SXEController;
import de.fu_berlin.inf.dpp.whiteboard.sxe.constants.SXEMessageType;

/**
 * <p>
 * Base class for the invitee for the synchronization process during invitation
 * </p>
 * 
 * *
 * <p>
 * Message flow:</br>
 * 
 * - send accept-state</br>
 * 
 * - wait for state</br>
 * </p>
 * 
 * @author jurke
 * 
 */
public class SXEIncomingSynchronizationProcess extends SXESynchronization {

	public static final Logger log = Logger
			.getLogger(SXEIncomingSynchronizationProcess.class);

	public SXEIncomingSynchronizationProcess(SXEController controller,
			ISXETransmitter sxe, SXEMessage stateOfferMessage) {
		super(controller, stateOfferMessage.getSession(), stateOfferMessage
				.getFrom());
		assert (stateOfferMessage.getMessageType() == SXEMessageType.STATE_OFFER);
	}

	/*
	 * TODO: At the moment started after receiving the state-offer, however,
	 * should start with connect message after session negotiation see SXE
	 * XEP-0284 but Saros used to lack a convenient place to send the connect
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXESynchronization#start(org.
	 * eclipse.core.runtime.SubMonitor)
	 */
	public void start(final SubMonitor monitor) {
		/*
		 * Has to run in another thread because else this would block Smack
		 * dispatching listener thread when awaiting answers (could not process
		 * incoming messages, deadlock)
		 */
		Utils.runSafeAsync("Incoming whiteboard synchronization process", log,
				new Runnable() {

					@Override
					public void run() {
						try {

							log.debug(prefix() + "invitation from " + peer
									+ " received");

							Utils.runSafeSWTSync(log, new Runnable() {

								@Override
								public void run() {
									if (!controller
											.switchToConnectingState(session)) {
										log.debug(prefix()
												+ "Received state offer while in "
												+ controller.getState()
												+ " state");
										SXEMessage msg = session
												.getNextMessage(
														SXEMessageType.REFUSE_STATE,
														peer);
										controller.getTransmitter().sendAsync(msg);
										return;
									}
								}
							});

							SXEMessage msg = session.getNextMessage(
									SXEMessageType.ACCEPT_STATE, peer);

							log.debug(prefix()
									+ "queue incoming records from now");

							final SXEMessage stateMessage = controller
									.getTransmitter().sendAndAwait(monitor,
											msg, SXEMessageType.STATE);

							log.debug(prefix() + "state received");

							Utils.runSafeSWTSync(log, new Runnable() {

								@Override
								public void run() {
									controller.startSession(stateMessage);
								}

							});

							// TODO send ack? Note: is not included in SXE

						} catch (IOException e) {
							log.error(prefix()
									+ "Timeout while synchronizing whiteboard state: "
									+ e.getMessage());
						} catch (LocalCancellationException lce) {
							log.debug(prefix()
									+ "Whitebaord synchronization cancelled: "
									+ lce.getMessage());
						} catch (Exception e) {
							log.debug(prefix()
									+ "Unexpected Exception in Whitebaord synchronization: "
									+ e);
						}
					}
				});

	}

	@Override
	protected String prefix() {
		return "SXE(" + session.getSessionId() + ") ";
	}

}