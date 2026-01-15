package com.github.standobyte.jojo.client.render.entity.animnew.molang;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javassist.ClassPool;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.ExpressionInterpreter;
import team.unnamed.mocha.runtime.MochaFunction;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.binding.JavaObjectBinding;
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction;
import team.unnamed.mocha.runtime.standard.MochaMath;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.Value;

public class MochaEngineWithoutJavassist<T> implements MochaEngine<T> {
    private final Scope scope;
    private final T entity;

    private Consumer<@NotNull ParseException> parseExceptionHandler;
    private boolean warnOnReflectiveFunctionUsage;

    public MochaEngineWithoutJavassist(final T entity, final Consumer<Scope.Builder> scopeBuilder) {
        Scope.Builder builder = Scope.builder();
        scopeBuilder.accept(builder);
        this.scope = builder.build();
        this.entity = entity;
    }

    @Override
    public double eval(final @NotNull List<Expression> expressions) {
        // create bindings that just apply for this evaluation
        final Scope local = scope.copy();
        {
            // create temp bindings
            MutableObjectBinding temp = new MutableObjectBinding();
            local.set("temp", temp);
            local.set("t", temp);
        }
        local.readOnly(true);
        ExpressionInterpreter<T> evaluator = new ExpressionInterpreter<>(entity, local);
        evaluator.warnOnReflectiveFunctionUsage(warnOnReflectiveFunctionUsage);
        Value lastResult = NumberValue.zero();

        for (Expression expression : expressions) {
            lastResult = expression.visit(evaluator);
            Value returnValue = evaluator.popReturnValue();
            if (returnValue != null) {
                lastResult = returnValue;
                break;
            }
        }

        // ensure returned value is a number
        return lastResult == null ? 0D : lastResult.getAsNumber();
    }

    @Override
    public double eval(final @NotNull Reader source) {
        final List<Expression> parsed;
        try {
            parsed = parse(source);
        } catch (final ParseException e) {
            // parse errors just output zero
            if (parseExceptionHandler != null) {
                parseExceptionHandler.accept(e);
            }
            return 0;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from given reader", e);
        }
        return eval(parsed);
    }

    @Override
    public @NotNull MochaFunction prepareEval(final @NotNull Reader reader) {
        final List<Expression> parsed;
        try {
            parsed = parse(reader);
        } catch (final ParseException e) {
            // parse errors just output zero
            if (parseExceptionHandler != null) {
                parseExceptionHandler.accept(e);
            }
            return () -> 0D;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read from given reader", e);
        }
        return new MochaFunction() {
            @Override
            public double evaluate() {
                return eval(parsed);
            }

            @Override
            public @NotNull String toString() {
                return "MochaPreparedFunction(" + parsed + ")";
            }
        };
    }

    @Override
    public @NotNull MochaFunction prepareEval(final @NotNull String code) {
        final List<Expression> parsed;
        try {
            parsed = parse(code);
        } catch (final ParseException e) {
            // parse errors just output zero
            if (parseExceptionHandler != null) {
                parseExceptionHandler.accept(e);
            }
            return new MochaFunction() {
                @Override
                public double evaluate() {
                    return 0D;
                }

                @Override
                public @NotNull String toString() {
                    return "MochaPreparedFunction('" + code + "', " + e.getMessage() + ")";
                }
            };
        }

        return new MochaFunction() {
            @Override
            public double evaluate() {
                return eval(parsed);
            }

            @Override
            public @NotNull String toString() {
                return "MochaPreparedFunction(" + parsed + ")";
            }
        };
    }

    @Override
    public <F extends MochaCompiledFunction> @NotNull F compile(final @NotNull Reader reader, final @NotNull Class<F> interfaceType) {
    	throw new UnsupportedOperationException("Failed to compile a Molang expression: CurseForge doesn't like javassist, so I can't use it :( Use prepareEval instead");
    }

    @Override
    public @NotNull ClassPool classPool() {
    	throw new UnsupportedOperationException("CurseForge doesn't like javassist, so I can't use it :(");
    }

    @Override
    public @NotNull Scope scope() {
        return scope;
    }

    @Override
    public void bind(final @NotNull Class<?> clazz) {
        final JavaObjectBinding javaObjectBinding = JavaObjectBinding.of(clazz, null, null);
        for (final String name : javaObjectBinding.names()) {
            scope.set(name, javaObjectBinding);
        }
    }

    @Override
    public <B> void bindInstance(final @NotNull Class<? super B> clazz, final @NotNull B instance, final @NotNull String name, final @NotNull String @NotNull ... aliases) {
        final JavaObjectBinding javaObjectBinding = JavaObjectBinding.of(clazz, instance, null);
        scope.set(name, javaObjectBinding);
        for (final String alias : aliases) {
            scope.set(alias, javaObjectBinding);
        }
    }

    @Override
    public @NotNull List<Expression> parse(final @NotNull Reader reader) throws IOException {
        return MolangParser.parser(reader).parseAll();
    }

    @Override
    public @NotNull MochaEngine<T> warnOnReflectiveFunctionUsage(final boolean warnOnReflectiveFunctionUsage) {
        this.warnOnReflectiveFunctionUsage = warnOnReflectiveFunctionUsage;
        return this;
    }

    @Override
    public @NotNull MochaEngine<T> handleParseExceptions(final @Nullable Consumer<@NotNull ParseException> exceptionHandler) {
        this.parseExceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public @NotNull MochaEngine<T> postCompile(final @Nullable Consumer<byte @NotNull []> bytecodeConsumer) {
    	throw new UnsupportedOperationException("CurseForge doesn't like javassist, so I can't use it :(");
    }


    public static <T> @NotNull MochaEngine<T> createStandard() {
    	return createStandard(null);
    }

    public static <T> @NotNull MochaEngine<T> createStandard(T entity) {
        return new MochaEngineWithoutJavassist<>(entity, builder -> {
            builder.set("math", JavaObjectBinding.of(MochaMath.class, null, new MochaMath()));
            final MutableObjectBinding variableBinding = new MutableObjectBinding();
            builder.set("variable", variableBinding);
            builder.set("v", variableBinding);
        });
    }

}
