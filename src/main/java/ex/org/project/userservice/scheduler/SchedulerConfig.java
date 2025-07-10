package ex.org.project.userservice.scheduler;

import ex.org.project.userservice.auth.ras.AuthRasService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    @Autowired
    Environment env;

    private final AuthRasService rasService;

    @Scheduled(fixedRate = 50, timeUnit = TimeUnit.MINUTES)
    public void scheduleFixedIntervalTask() {
       rasService.pingRasPassportService();
    }
}
