package com.ts.music.action;

/**
 * BindingCommand.
 */
public class BindingCommand<T> {
    private BindingAction mExecute;
    private BindingConsumer<T> mConsumer;
    private BindingFunction<Boolean> mCanExecute;

    public BindingCommand(BindingAction execute) {
        mExecute = execute;
    }

    /**
     * Binding with parameters.
     */
    public BindingCommand(BindingConsumer<T> execute) {
        mConsumer = execute;
    }

    /**
     * Binding with parameters.
     */
    public BindingCommand(BindingAction execute, BindingFunction<Boolean> canExecute0) {
        mExecute = execute;
        mCanExecute = canExecute0;
    }

    /**
     * binding_with_parameters.
     */
    public BindingCommand(BindingConsumer<T> execute, BindingFunction<Boolean> canExecute0) {
        mConsumer = execute;
        mCanExecute = canExecute0;
    }

    /**
     * Binding with parameters.
     */
    public void execute() {
        if (mExecute != null && canExecute0()) {
            mExecute.call();
        }
    }

    /**
     * Binding with parameters.
     */
    public void execute(T parameter) {
        if (mConsumer != null && canExecute0()) {
            mConsumer.call(parameter);
        }
    }

    /**
     * Binding with parameters.
     */
    private boolean canExecute0() {
        if (mCanExecute == null) {
            return true;
        }
        return mCanExecute.call();
    }
}
