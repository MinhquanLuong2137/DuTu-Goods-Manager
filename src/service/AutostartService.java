package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AutostartService {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_VALUE = "DuTuGoodsManager";
    private static final String DESKTOP_FILE = "dutu-goods-manager.desktop";

    public boolean isEnabled() throws IOException, InterruptedException {
        String os = operatingSystem();
        if (os.equals("windows")) {
            return runCommand("reg.exe", "query", RUN_KEY, "/v", RUN_VALUE).exitCode() == 0;
        }
        return Files.isRegularFile(desktopFile());
    }

    public void setEnabled(boolean enabled) throws IOException, InterruptedException {
        String os = operatingSystem();
        if (os.equals("windows")) {
            setWindowsEnabled(enabled);
        } else {
            setLinuxEnabled(enabled);
        }
    }

    private void setWindowsEnabled(boolean enabled) throws IOException, InterruptedException {
        if (!enabled) {
            if (runCommand("reg.exe", "query", RUN_KEY, "/v", RUN_VALUE).exitCode() != 0) return;
            CommandResult result = runCommand("reg.exe", "delete", RUN_KEY, "/v", RUN_VALUE, "/f");
            if (result.exitCode() != 0) {
                throw new IOException("Could not remove the Windows autostart entry: " + result.output());
            }
            return;
        }

        String command = resolveLaunchArguments().stream()
                .map(AutostartService::quoteWindowsArgument)
                .reduce((first, second) -> first + " " + second)
                .orElseThrow(() -> new IOException("Could not locate the application launcher."));
        CommandResult result = runCommand("reg.exe", "add", RUN_KEY, "/v", RUN_VALUE,
                "/t", "REG_SZ", "/d", command, "/f");
        if (result.exitCode() != 0) {
            throw new IOException("Could not register Windows autostart: " + result.output());
        }
    }

    private void setLinuxEnabled(boolean enabled) throws IOException {
        Path file = desktopFile();
        if (!enabled) {
            Files.deleteIfExists(file);
            return;
        }

        Files.createDirectories(file.getParent());
        String command = resolveLaunchArguments().stream()
                .map(AutostartService::quoteDesktopArgument)
                .reduce((first, second) -> first + " " + second)
                .orElseThrow(() -> new IOException("Could not locate the application launcher."));
        String desktopEntry = """
                [Desktop Entry]
                Type=Application
                Name=DuTu Goods Manager
                Exec=%s
                Terminal=false
                X-GNOME-Autostart-enabled=true
                """.formatted(command);
        Files.writeString(file, desktopEntry, StandardCharsets.UTF_8);
    }

    private List<String> resolveLaunchArguments() throws IOException {
        String packagedLauncher = System.getProperty("jpackage.app-path");
        if (packagedLauncher != null && !packagedLauncher.isBlank()) {
            return List.of(Path.of(packagedLauncher).toAbsolutePath().normalize().toString());
        }

        try {
            Path location = Path.of(AutostartService.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            String javaExecutable = Path.of(System.getProperty("java.home"), "bin",
                    operatingSystem().equals("windows") ? "javaw.exe" : "java").toString();
            if (Files.isRegularFile(location) && location.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                return List.of(javaExecutable, "-cp", location + System.getProperty("path.separator")
                        + location.getParent() + System.getProperty("file.separator") + "*", "Main");
            }
            return List.of(javaExecutable, "-cp", System.getProperty("java.class.path"), "Main");
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Could not locate the application launcher.", e);
        }
    }

    private static Path desktopFile() {
        return Path.of(System.getProperty("user.home"), ".config", "autostart", DESKTOP_FILE);
    }

    private static String quoteDesktopArgument(String argument) {
        return "\"" + argument.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("`", "\\`")
                .replace("$", "\\$") + "\"";
    }

    private static String quoteWindowsArgument(String argument) {
        return "\"" + argument.replace("\"", "\\\"") + "\"";
    }

    private static String operatingSystem() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) return "windows";
        if (os.contains("linux")) return "linux";
        throw new IOException("Autostart is supported only on Windows and Linux.");
    }

    private static CommandResult runCommand(String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            return new CommandResult(process.waitFor(), output);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private record CommandResult(int exitCode, String output) {}
}
