package mmielus.payload;
import javax.validation.constraints.NotNull;

public class VoteRequest {
    private @NotNull Long choiceId;

    public Long getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Long choiceId) {
        this.choiceId = choiceId;
    }
}

