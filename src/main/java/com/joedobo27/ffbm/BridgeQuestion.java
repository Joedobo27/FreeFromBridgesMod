package com.joedobo27.ffbm;

import com.joedobo27.libs.bml.BmlForm;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import org.gotti.wurmunlimited.modsupport.questions.ModQuestion;
import org.gotti.wurmunlimited.modsupport.questions.ModQuestions;

import java.util.Properties;
import java.util.logging.Level;

public class BridgeQuestion implements ModQuestion {

    private final int questionType;
    private String answer = null;

    BridgeQuestion(Creature responder, String title, String question, int type, long aTarget) {
        this.questionType = type;
        sendQuestion(ModQuestions.createQuestion(responder, title, question, aTarget, this));
    }

    @Override
    public int getType() {
        return this.questionType;
    }

    @Override
    public void answer(Question question, Properties properties) {
        if (question.getType() == 0) {
            FreeFromBridgesMod.logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.getType() == question.getType()) {
            String answer = properties.getProperty("itemJson", null);
            if (answer != null)
                this.answer = answer;
        }
    }

    @Override
    public void sendQuestion(Question question) {
        BmlForm bmlForm = new BmlForm(question.getTitle(), 500, 500);
        bmlForm.addHidden("id", Integer.toString(question.getId()));
        bmlForm.addLargeInput("itemJson", "", 1000, "200,200,200", -1);
        bmlForm.addButton("Send", "submit");
        String bml = bmlForm.toString();
        question.getResponder().getCommunicator().sendBml(500, 500, true, true,
                bml, 200, 200, 200, question.getTitle());
    }

    boolean isAnswered() {
        return this.answer != null;
    }

    String getAnswer() {
        return this.answer;
    }
}
