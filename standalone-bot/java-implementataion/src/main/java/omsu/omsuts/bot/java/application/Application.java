package omsu.omsuts.bot.java.application;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Scanner;

/**
 * Created by sds on 6/12/16.
 */
@Slf4j
public class Application implements Runnable {

    @Getter
    private ApplicationComponent applicationComponent;

    public Application() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        applicationComponent.inject(this);
    }

    @Override
    public void run() {
        val in = new Scanner(System.in);
        do{
            System.out.println("Command:");
            val cmd = in.nextLine();
            if ("exit".equals(cmd)) {
                break;
            }
            log.error("Invalid command: '{}'", cmd);
        } while (true);
    }
}
