package utils;

import animatefx.animation.*;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Centralized animation helper for the Collaboration module.
 * Uses the AnimateFX library to provide smooth, premium animations
 * for view transitions, list items, forms, and dashboard elements.
 */
public class AnimationUtils {

    // ─── Default Speeds ──────────────────────────────────────────────
    private static final double VIEW_TRANSITION_SPEED = 1.4;
    private static final double LIST_ITEM_SPEED = 1.6;
    private static final double FORM_SPEED = 1.3;
    private static final double STAT_CARD_SPEED = 1.5;
    private static final double STAGGER_DELAY_MS = 60;

    // ─── View Transitions (loading new content into a StackPane) ─────

    /**
     * Animate a newly loaded view sliding in with a fade effect.
     * Best for: swapping content inside a StackPane contentArea.
     */
    public static void animateViewTransition(Node view) {
        if (view == null) return;
        view.setOpacity(0);
        new FadeIn(view).setSpeed(VIEW_TRANSITION_SPEED).play();
        new SlideInUp(view).setSpeed(VIEW_TRANSITION_SPEED).play();
    }

    /**
     * Animate a view sliding in from the right (for drill-down navigation).
     */
    public static void animateSlideInRight(Node view) {
        if (view == null) return;
        view.setOpacity(0);
        new FadeIn(view).setSpeed(VIEW_TRANSITION_SPEED).play();
        new SlideInRight(view).setSpeed(VIEW_TRANSITION_SPEED).play();
    }

    /**
     * Animate a view sliding in from the left (for back navigation).
     */
    public static void animateSlideInLeft(Node view) {
        if (view == null) return;
        view.setOpacity(0);
        new FadeIn(view).setSpeed(VIEW_TRANSITION_SPEED).play();
        new SlideInLeft(view).setSpeed(VIEW_TRANSITION_SPEED).play();
    }

    // ─── List / Collection Animations ────────────────────────────────

    /**
     * Animate all children of a Pane with staggered FadeInUp.
     * Best for: VBox of stat cards, dashboard rows, list items.
     */
    public static void animateChildren(Pane parent) {
        if (parent == null) return;
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            child.setOpacity(0);
            final int index = i;
            PauseTransition delay = new PauseTransition(Duration.millis(STAGGER_DELAY_MS * index));
            delay.setOnFinished(e -> {
                child.setOpacity(1);
                new FadeInUp(child).setSpeed(LIST_ITEM_SPEED).play();
            });
            delay.play();
        }
    }

    /**
     * Animate a single list cell node when it appears.
     * Lighter weight than animating all children at once.
     */
    public static void animateListCell(Node cellNode) {
        if (cellNode == null) return;
        new FadeIn(cellNode).setSpeed(LIST_ITEM_SPEED).play();
    }

    // ─── Form Animations ─────────────────────────────────────────────

    /**
     * Animate a form/detail view appearing with a zoom + fade effect.
     * Best for: add/update forms, detail views.
     */
    public static void animateFormIn(Node formRoot) {
        if (formRoot == null) return;
        formRoot.setOpacity(0);
        new FadeIn(formRoot).setSpeed(FORM_SPEED).play();
        new ZoomIn(formRoot).setSpeed(FORM_SPEED).play();
    }

    /**
     * Animate form fields staggered from top to bottom.
     * Best for: form containers with multiple field groups.
     */
    public static void animateFormFields(Pane formContainer) {
        if (formContainer == null) return;
        for (int i = 0; i < formContainer.getChildren().size(); i++) {
            Node child = formContainer.getChildren().get(i);
            child.setOpacity(0);
            final int index = i;
            PauseTransition delay = new PauseTransition(Duration.millis(80 * index));
            delay.setOnFinished(e -> {
                child.setOpacity(1);
                new FadeInUp(child).setSpeed(FORM_SPEED).play();
            });
            delay.play();
        }
    }

    // ─── Dashboard / Stat Card Animations ────────────────────────────

    /**
     * Animate dashboard stat cards with a staggered bounce-in effect.
     */
    public static void animateStatCards(Pane container) {
        if (container == null) return;
        for (int i = 0; i < container.getChildren().size(); i++) {
            Node child = container.getChildren().get(i);
            child.setOpacity(0);
            final int index = i;
            PauseTransition delay = new PauseTransition(Duration.millis(100 * index));
            delay.setOnFinished(e -> {
                child.setOpacity(1);
                new BounceIn(child).setSpeed(STAT_CARD_SPEED).play();
            });
            delay.play();
        }
    }

    /**
     * Animate a hero banner section fading in.
     */
    public static void animateHeroBanner(Node banner) {
        if (banner == null) return;
        banner.setOpacity(0);
        new FadeIn(banner).setSpeed(1.0).play();
    }

    // ─── Micro-interactions ──────────────────────────────────────────

    /**
     * Quick pulse animation for interactive feedback (e.g., button click).
     */
    public static void pulse(Node node) {
        if (node == null) return;
        new Pulse(node).setSpeed(2.0).play();
    }

    /**
     * Shake animation for error feedback (e.g., validation failure).
     */
    public static void shake(Node node) {
        if (node == null) return;
        new Shake(node).setSpeed(2.5).play();
    }

    /**
     * Flash animation for attention (e.g., status change).
     */
    public static void flash(Node node) {
        if (node == null) return;
        new Flash(node).setSpeed(2.0).play();
    }

    /**
     * Subtle bounce effect for a node (e.g., a save success).
     */
    public static void bounceIn(Node node) {
        if (node == null) return;
        new BounceIn(node).setSpeed(1.5).play();
    }
}
