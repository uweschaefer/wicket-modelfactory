package org.wicketeer.modelfactory;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.wicket.util.tester.WicketTester;

public class ConcurrentModelFactoryStorm {

    static class A implements Serializable {
        private static final long serialVersionUID = 1L;
        B b;

        public B getB() {
            return this.b;
        }

        public void setB(final B b) {
            this.b = b;
        }
    }

    enum Enamm {
        A, B, C
    }

    static class B implements Serializable {

        private static final long serialVersionUID = 1L;

        String s;

        UUID u = UUID.randomUUID();

        Boolean b = true;

        Enamm e = Enamm.C;

        public Enamm getE() {
            return this.e;
        }

        public void setE(final Enamm e) {
            this.e = e;
        }

        public Boolean isB() {
            return this.b;
        }

        public void setB(final Boolean b) {
            this.b = b;
        }

        public UUID getU() {
            return this.u;
        }

        public void setU(final UUID u) {
            this.u = u;
        }

        public String getS() {
            return this.s;
        }

        public void setS(final String s) {
            this.s = s;
        }
    }

    static class R1 implements Runnable {

        @Override
        public void run() {

            // RequestCycle c = new RequestCycle(null);
            // ThreadContext.setRequestCycle(c);

            try {
                WicketTester t = new WicketTester();

                try {
                    Thread.currentThread();
                    Thread.sleep((long) Math.random() * 50);
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (Math.random() > .5) {
                    String b = ModelFactory
                            .path(ModelFactory.from(new A()).getB());
                    if (!b.equals("b")) {
                        System.out.println("NARF " + b);
                    }
                }

                {
                    String path = ModelFactory
                            .path(ModelFactory.from(new A()).getB().getS());
                    if (!"b.s".equals(path)) {
                        System.out.println("POIT " + path);
                    }
                }

                {
                    String path = ModelFactory
                            .path(ModelFactory.from(new A()).getB().getU());
                    if (!"b.u".equals(path)) {
                        System.out.println("UUID " + path);
                    }
                }

                {
                    String path = ModelFactory
                            .path(ModelFactory.from(new A()).getB().getE());
                    if (!"b.e".equals(path)) {
                        System.out.println("Enum " + path);
                    }
                }

                {
                    String path = ModelFactory
                            .path(ModelFactory.from(new A()).getB().isB());
                    if (!"b.b".equals(path)) {
                        System.out.println("bool " + path);
                    }
                }

                t.destroy();
            }
            catch (Throwable e) {
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                System.out.println(
                        "**************************************************");
                e.printStackTrace();
            }
        }
    }

    private static final int MAX = 1000 * 12;

    public static void main(final String[] args) {

        for (int j = 0; j < 2000; j++) {
            ExecutorService e = Executors.newFixedThreadPool(2000);
            for (int i = 0; i < 2; i++) {
                e.submit(new R1());
            }

            e.shutdown();
            try {
                e.awaitTermination(2, TimeUnit.MINUTES);
            }
            catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        long start = System.currentTimeMillis();
        for (int j = 0; j < ConcurrentModelFactoryStorm.MAX; j++) {
            ExecutorService e = Executors.newFixedThreadPool(2000);
            for (int i = 0; i < 2; i++) {
                e.submit(new R1());
            }

            e.shutdown();
            try {
                e.awaitTermination(2, TimeUnit.MINUTES);
            }
            catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        System.out.println("rt " + (System.currentTimeMillis() - start));

        // new WicketTester();
        // String path = ModelFactory.path(ModelFactory.from(new
        // A()).getB().getE());
        // if (!path.equals("b.e"))
        // {
        // System.out.println("Enum " + path);
        // }
    }

}
