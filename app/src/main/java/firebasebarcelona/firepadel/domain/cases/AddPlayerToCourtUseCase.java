package firebasebarcelona.firepadel.domain.cases;

import firebasebarcelona.firepadel.data.courts.repository.CourtRepository;
import firebasebarcelona.firepadel.domain.cases.callbacks.GetCourtCallback;
import firebasebarcelona.firepadel.domain.exceptions.PlayerInAnotherCourtException;
import firebasebarcelona.firepadel.domain.models.Court;
import firebasebarcelona.firepadel.domain.models.Player;
import javax.inject.Inject;

public class AddPlayerToCourtUseCase extends AbstractUseCase {
  private final IsPlayerInAnotherCourtUseCase isPlayerInAnotherCourtUseCase;
  private final CourtRepository courtRepository;
  private String courtId;
  private Player player;
  private Callback callback;

  @Inject
  public AddPlayerToCourtUseCase(IsPlayerInAnotherCourtUseCase isPlayerInAnotherCourtUseCase, CourtRepository courtRepository) {
    this.isPlayerInAnotherCourtUseCase = isPlayerInAnotherCourtUseCase;
    this.courtRepository = courtRepository;
  }

  public void execute(String courtId, Player player, Callback callback) {
    this.courtId = courtId;
    this.player = player;
    this.callback = callback;
    run();
  }

  @Override
  protected void onRun() {
    isPlayerInAnotherCourtUseCase.execute(courtId, player, new IsPlayerInAnotherCourtUseCase.Callback() {
      @Override
      public void onPlayerIsInSomeCourt() {
        callback.onAddPlayerToCourtFailed(new PlayerInAnotherCourtException());
      }

      @Override
      public void onPlayerIsNotInAnyCourt() {
        courtRepository.addPlayerToCourt(courtId, player);
        courtRepository.getCourt(courtId, new GetCourtCallback() {
          @Override
          public void onGetCourtSuccess(Court court) {
            callback.onAddPlayerToCourtSuccess(court);
          }
        });
      }
    });
  }

  public interface Callback {
    void onAddPlayerToCourtSuccess(Court court);
    void onAddPlayerToCourtFailed(PlayerInAnotherCourtException exception);
  }
}
