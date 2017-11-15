package org.travelbot.java.triggers;

import org.joo.scorpius.support.TriggerExecutionException;
import org.joo.scorpius.trigger.AbstractTrigger;
import org.joo.scorpius.trigger.TriggerExecutionContext;
import org.travelbot.java.MessengerApplicationContext;
import org.travelbot.java.dto.MessengerEvent;
import org.travelbot.java.dto.MessengerResponse;

import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.Payload;
import com.github.messenger4j.send.message.TextMessage;

public class MessageReceivedTrigger extends AbstractTrigger<MessengerEvent, MessengerResponse> {

	@Override
	public void execute(TriggerExecutionContext executionContext) throws TriggerExecutionException {
		MessengerApplicationContext applicationContext = (MessengerApplicationContext) executionContext.getApplicationContext();
		MessengerEvent event = (MessengerEvent) executionContext.getRequest();
		
		final String recipientId = event.getOriginalEvent().senderId();
		final String text = event.getOriginalEvent().asTextMessageEvent().text();
		final Payload payload = MessagePayload.create(recipientId, TextMessage.create(text));
		
		try {
			applicationContext.getMessenger().send(payload);
		} catch (MessengerApiException | MessengerIOException e) {
			throw new TriggerExecutionException(e);
		}
		executionContext.finish(new MessengerResponse());
	}
}