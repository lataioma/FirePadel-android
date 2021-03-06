package firebasebarcelona.firepadel.ui.chat.presentation;

import firebasebarcelona.firepadel.app.rx.AbsSubscriber;
import firebasebarcelona.firepadel.domain.cases.GetChatMessagesByCourtIdUseCase;
import firebasebarcelona.firepadel.domain.cases.GetLocalPlayerUseCase;
import firebasebarcelona.firepadel.domain.cases.SendMessageUseCase;
import firebasebarcelona.firepadel.domain.cases.SubscribeToChatByCourtIdUseCase;
import firebasebarcelona.firepadel.domain.models.Message;
import firebasebarcelona.firepadel.domain.models.Player;
import firebasebarcelona.firepadel.ui.models.MessagesViewModelMapper;
import firebasebarcelona.firepadel.ui.models.PlayerViewModelMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

public class ChatPresenter {
  private final ChatView view;
  private final GetChatMessagesByCourtIdUseCase getChatMessagesByCourtIdUseCase;
  private final MessagesViewModelMapper mapper;

  private GetChatMessagesByCourtIdUseCase.OnMessagesReadyCallback callback;
  private String courtId;
  private SendMessageUseCase sendMessageUseCase;
  private final GetLocalPlayerUseCase getLocalPlayerUseCase;
  private PlayerViewModelMapper playerMapper;
  private final SubscribeToChatByCourtIdUseCase subscribeToChatByCourtIdUseCase;
  private Player myPlayer;

  @Inject
  public ChatPresenter(ChatView view, GetChatMessagesByCourtIdUseCase getChatMessagesByCourtIdUseCase,
                      MessagesViewModelMapper mapper, SendMessageUseCase sendMessageUseCase, PlayerViewModelMapper playerMapper,
                      GetLocalPlayerUseCase getLocalPlayerUseCase,
                      SubscribeToChatByCourtIdUseCase subscribeToChatByCourtIdUseCase) {
    this.view = view;
    this.getChatMessagesByCourtIdUseCase = getChatMessagesByCourtIdUseCase;
    this.mapper = mapper;
    this.sendMessageUseCase = sendMessageUseCase;
    this.getLocalPlayerUseCase = getLocalPlayerUseCase;
    this.playerMapper = playerMapper;
    this.subscribeToChatByCourtIdUseCase = subscribeToChatByCourtIdUseCase;
  }

  public void requestToChat(String courtId) {
    this.courtId = courtId;
    fetchLocalPlayer();
  }

  public void unsubscribeToChat() {
    subscribeToChatByCourtIdUseCase.unsuscribe();
  }

  private void fetchLocalPlayer() {
    getLocalPlayerUseCase.execute(new GetLocalPlayerUseCase.Callback() {
      @Override
      public void onGetLocalPlayerSuccess(Player player) {
        myPlayer = player;
        view.renderList(playerMapper.map(player));
        subscribeToChat();
      }

      @Override
      public void onGetLocalPlayerError() {
      }
    });
  }

  private void subscribeToChat() {
    subscribeToChatByCourtIdUseCase.execute(courtId, new AbsSubscriber<List<Message>>() {
      @Override
      public void onNext(List<Message> messages) {
        view.updateMessages(mapper.map(invert(messages)));
      }
    });
  }

  private List<Message> invert(List<Message> messages) {
    List<Message> messagesToBeInverted = new ArrayList<>(messages);
    Collections.reverse(messagesToBeInverted);
    return messagesToBeInverted;
  }

  public void onSendMessageClick(String message) {
    sendMessageUseCase.execute(courtId, message);
    view.clearMessageToBeSend();
  }
}
