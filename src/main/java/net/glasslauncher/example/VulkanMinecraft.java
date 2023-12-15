//package net.glasslauncher.example;
//
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.minecraft.achievement.Achievements;
//import net.minecraft.applet.GameStartupErrorPanel;
//import net.minecraft.class_556;
//import net.minecraft.class_596;
//import net.minecraft.class_66;
//import net.minecraft.client.GameStartupError;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.MinecraftApplet;
//import net.minecraft.client.TexturePackManager;
//import net.minecraft.client.gui.InGame;
//import net.minecraft.client.gui.screen.Connecting;
//import net.minecraft.client.gui.screen.LevelSaveConflict;
//import net.minecraft.client.gui.screen.OutOfMemory;
//import net.minecraft.client.gui.screen.menu.MainMenu;
//import net.minecraft.client.options.GameOptions;
//import net.minecraft.client.particle.ParticleManager;
//import net.minecraft.client.render.*;
//import net.minecraft.client.render.block.BlockRenderer;
//import net.minecraft.client.render.block.FoliageColour;
//import net.minecraft.client.render.block.GrassColour;
//import net.minecraft.client.render.block.WaterColour;
//import net.minecraft.client.render.entity.EntityRenderDispatcher;
//import net.minecraft.client.texture.TextureManager;
//import net.minecraft.client.util.OcclusionQueryTester;
//import net.minecraft.client.util.ThreadDownloadResources;
//import net.minecraft.level.Level;
//import net.minecraft.level.storage.McRegionLevelStorage;
//import net.minecraft.level.storage.SessionLockException;
//import net.minecraft.sortme.GameRenderer;
//import net.minecraft.util.ProgressListenerError;
//import net.minecraft.util.io.StatsFileWriter;
//import net.minecraft.util.maths.Box;
//import net.minecraft.util.maths.Vec3f;
//import org.lwjgl.LWJGLException;
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.glfw.GLFW;
//import org.lwjgl.glfw.GLFWVulkan;
//import org.lwjgl.input.Controllers;
//import org.lwjgl.input.Keyboard;
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.Display;
//import org.lwjgl.opengl.DisplayMode;
//import org.lwjgl.opengl.GL11;
//import org.lwjgl.system.Configuration;
//import org.lwjgl.system.MemoryStack;
//import org.lwjgl.system.MemoryUtil;
//import org.lwjgl.util.glu.GLU;
//import org.lwjgl.vulkan.*;
//
//import java.awt.*;
//import java.io.File;
//import java.nio.IntBuffer;
//import java.nio.LongBuffer;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class VulkanMinecraft extends Minecraft {
//    public static final String MC_VERSION = "Minecraft Beta 1.7.3";
//
//    private static final boolean ENABLE_VALIDATION_LAYERS = false;//Configuration.DEBUG.get(true);
//
//    private static final Set<String> VALIDATION_LAYERS;
//    static {
//        if(ENABLE_VALIDATION_LAYERS) {
//            VALIDATION_LAYERS = new HashSet<>();
//            VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
//        } else {
//            // We are not going to use it, so we don't create it
//            VALIDATION_LAYERS = null;
//        }
//    }
//
//    private VkInstance instance;
//    private VkPhysicalDevice physicalDevice;
//    private long window;
//    private long debugMessenger;
//
//    public VulkanMinecraft(Component component, Canvas canvas, MinecraftApplet minecraftApplet, int i, int j, boolean bl) {
//        super(component, canvas, minecraftApplet, i, j, bl);
//    }
//
//    private void setupDebugMessenger() {
//
//        if(!ENABLE_VALIDATION_LAYERS) {
//            return;
//        }
//
//        try(MemoryStack stack = MemoryStack.stackPush()) {
//
//            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
//
//            populateDebugMessengerCreateInfo(createInfo);
//
//            LongBuffer pDebugMessenger = stack.longs(VK13.VK_NULL_HANDLE);
//
//            if(createDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger) != VK13.VK_SUCCESS) {
//                throw new RuntimeException("Failed to set up debug messenger");
//            }
//
//            debugMessenger = pDebugMessenger.get(0);
//        }
//    }
//
//    private void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
//        debugCreateInfo.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
//        debugCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
//        debugCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
//        debugCreateInfo.pfnUserCallback(VulkanMinecraft::debugCallback);
//    }
//
//    private PointerBuffer getRequiredExtensions(MemoryStack stack) {
//
//        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
//
//        if(ENABLE_VALIDATION_LAYERS) {
//
//            PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);
//
//            extensions.put(glfwExtensions);
//            extensions.put(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
//
//            // Rewind the buffer before returning it to reset its position back to 0
//            return extensions.rewind();
//        }
//
//        return glfwExtensions;
//    }
//
//    private boolean checkValidationLayerSupport() {
//
//        try(MemoryStack stack = MemoryStack.stackPush()) {
//
//            IntBuffer layerCount = stack.ints(0);
//
//            VK13.vkEnumerateInstanceLayerProperties(layerCount, null);
//
//            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);
//
//            VK13.vkEnumerateInstanceLayerProperties(layerCount, availableLayers);
//
//            Set<String> availableLayerNames = availableLayers.stream()
//                    .map(VkLayerProperties::layerNameString)
//                    .collect(Collectors.toSet());
//
//            return availableLayerNames.containsAll(VALIDATION_LAYERS);
//        }
//    }
//
//    public void setupVulkan() {
//        if(ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport()) {
//            throw new RuntimeException("Validation requested but not supported");
//        }
//
//        try(MemoryStack stack = MemoryStack.stackPush()) {
//
//            // Use calloc to initialize the structs with 0s. Otherwise, the program can crash due to random values
//
//            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);
//
//            appInfo.sType(VK13.VK_STRUCTURE_TYPE_APPLICATION_INFO);
//            appInfo.pApplicationName(stack.UTF8Safe(MC_VERSION));
//            appInfo.applicationVersion(VK13.VK_MAKE_VERSION(1, 0, 0));
//            appInfo.pEngineName(stack.UTF8Safe("No Engine"));
//            appInfo.engineVersion(VK13.VK_MAKE_VERSION(1, 0, 0));
//            appInfo.apiVersion(VK13.VK_API_VERSION_1_0);
//
//            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);
//
//            createInfo.sType(VK13.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
//            createInfo.pApplicationInfo(appInfo);
//            // enabledExtensionCount is implicitly set when you call ppEnabledExtensionNames
//            createInfo.ppEnabledExtensionNames(getRequiredExtensions(stack));
//            // same with enabledLayerCount
//            if(ENABLE_VALIDATION_LAYERS) {
//
//                createInfo.ppEnabledLayerNames(validationLayersAsPointerBuffer(stack));
//
//                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
//                populateDebugMessengerCreateInfo(debugCreateInfo);
//                createInfo.pNext(debugCreateInfo.address());
//            }
//
//            // We need to retrieve the pointer of the created instance
//            PointerBuffer instancePtr = stack.mallocPointer(1);
//
//            if(VK13.vkCreateInstance(createInfo, null, instancePtr) != VK13.VK_SUCCESS) {
//                throw new RuntimeException("Failed to create instance");
//            }
//
//            instance = new VkInstance(instancePtr.get(0), createInfo);
//        }
//
//        setupDebugMessenger();
//        pickPhysicalDevice();
//    }
//
//    private void initWindow() {
//
//        if(!GLFW.glfwInit()) {
//            throw new RuntimeException("Cannot initialize GLFW");
//        }
//
//        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
//        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
////        VK13.vkEnumerateInstanceExtensionProperties(MemoryUtil.NULL, )
//
////        String title = getClass().getEnclosingClass().getSimpleName();
//
//        window = GLFW.glfwCreateWindow(this.actualWidth, this.actualHeight, MC_VERSION, MemoryUtil.NULL, MemoryUtil.NULL);
//
//        if(window == MemoryUtil.NULL) {
//            throw new RuntimeException("Cannot create window");
//        }
//    }
//
//    private PointerBuffer validationLayersAsPointerBuffer(MemoryStack stack) {
//
//        PointerBuffer buffer = stack.mallocPointer(VALIDATION_LAYERS.size());
//
//        VALIDATION_LAYERS.stream()
//                .map(stack::UTF8)
//                .forEach(buffer::put);
//
//        return buffer.rewind();
//    }
//
//    private void pickPhysicalDevice() {
//
//        try(MemoryStack stack = MemoryStack.stackPush()) {
//
//            IntBuffer deviceCount = stack.ints(0);
//
//            VK13.vkEnumeratePhysicalDevices(instance, deviceCount, null);
//
//            if(deviceCount.get(0) == 0) {
//                throw new RuntimeException("Failed to find GPUs with Vulkan support");
//            }
//
//            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
//
//            VK13.vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);
//
//            for(int i = 0;i < ppPhysicalDevices.capacity();i++) {
//
//                VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);
//
//                if(isDeviceSuitable(device)) {
//                    physicalDevice = device;
//                    return;
//                }
//            }
//
//            throw new RuntimeException("Failed to find a suitable GPU");
//        }
//    }
//
//    private QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {
//
//        QueueFamilyIndices indices = new QueueFamilyIndices();
//
//        try(MemoryStack stack = MemoryStack.stackPush()) {
//
//            IntBuffer queueFamilyCount = stack.ints(0);
//
//            VK13.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
//
//            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);
//
//            VK13.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);
//
//            IntStream.range(0, queueFamilies.capacity())
//                    .filter(index -> (queueFamilies.get(index).queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0)
//                    .findFirst()
//                    .ifPresent(index -> indices.graphicsFamily = index);
//
//            return indices;
//        }
//    }
//
//    private boolean isDeviceSuitable(VkPhysicalDevice device) {
//
//        QueueFamilyIndices indices = findQueueFamilies(device);
//
//        return indices.isComplete();
//    }
//
//    private class QueueFamilyIndices {
//
//        // We use Integer to use null as the empty value
//        private Integer graphicsFamily;
//
//        private boolean isComplete() {
//            return graphicsFamily != null;
//        }
//
//    }
//
//    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {
//
//        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
//
//        System.err.println("Validation layer: " + callbackData.pMessageString());
//
//        return VK13.VK_FALSE;
//    }
//
//    private static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo,
//                                                    VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger) {
//
//        if(VK13.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != MemoryUtil.NULL) {
//            return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
//        }
//
//        return VK13.VK_ERROR_EXTENSION_NOT_PRESENT;
//    }
//
//    private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks) {
//
//        if(VK13.vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != MemoryUtil.NULL) {
//            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
//        }
//
//    }
//
//    @Override
//    public void init() {
//        initWindow();
//
//        setupVulkan();
//
//        this.gameDir = getGameDirectory();
//        this.levelStorage = new McRegionLevelStorage(new File(this.gameDir, "saves"));
//        this.options = new GameOptions(this, this.gameDir);
//        this.texturePackManager = new TexturePackManager(this, this.gameDir);
//        this.textureManager = new TextureManager(this.texturePackManager, this.options);
////        this.textRenderer = new TextRenderer(this.options, "/font/default.png", this.textureManager);
//        WaterColour.set(this.textureManager.getColorMap("/misc/watercolor.png"));
//        GrassColour.set(this.textureManager.getColorMap("/misc/grasscolor.png"));
//        FoliageColour.set(this.textureManager.getColorMap("/misc/foliagecolor.png"));
//        this.gameRenderer = new GameRenderer(this);
//        EntityRenderDispatcher.INSTANCE.field_2494 = new class_556(this);
//        this.statFileWriter = new StatsFileWriter(this.session, this.gameDir);
//        Achievements.OPEN_INVENTORY.setDescriptionFormat(new class_637());
////            this.method_2150();
////            Keyboard.create();
////            Mouse.create();
//        this.field_2767 = new class_596(this.canvas);
//
//        try {
//            Controllers.create();
//        } catch (Exception var4) {
//            var4.printStackTrace();
//        }
//
//
//        this.printVulkanError("Pre startup");
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glShadeModel(GL11.GL_SMOOTH);
//        GL11.glClearDepth(1.0);
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
//        GL11.glDepthFunc(GL11.GL_LEQUAL);
//        GL11.glEnable(GL11.GL_ALPHA_TEST);
//        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
//        GL11.glCullFace(GL11.GL_BACK);
//        GL11.glMatrixMode(GL11.GL_PROJECTION);
//        GL11.glLoadIdentity();
//        GL11.glMatrixMode(GL11.GL_MODELVIEW);
//        this.printVulkanError("Startup");
//        this.occlusionQueryTester = new OcclusionQueryTester();
//        this.soundHelper.acceptOptions(this.options);
//        this.textureManager.addTextureBinder(this.lavaTextureBinder);
//        this.textureManager.addTextureBinder(this.waterTextureBinder);
//        this.textureManager.addTextureBinder(new PortalTextureBinder());
//        this.textureManager.addTextureBinder(new CompassTextureBinder(this));
//        this.textureManager.addTextureBinder(new ClockTextureBinder(this));
//        this.textureManager.addTextureBinder(new FlowingWaterTextureBinder());
//        this.textureManager.addTextureBinder(new FlowingLavaTextureBinder2());
//        this.textureManager.addTextureBinder(new FireTextureBinder(0));
//        this.textureManager.addTextureBinder(new FireTextureBinder(1));
//        this.worldRenderer = new WorldRenderer(this, this.textureManager);
//        GL11.glViewport(0, 0, this.actualWidth, this.actualHeight);
//        this.particleManager = new ParticleManager(this.level, this.textureManager);
//
//        try {
//            this.resourceDownloadThread = new ThreadDownloadResources(this.gameDir, this);
//            this.resourceDownloadThread.start();
//        } catch (Exception var3) {
//        }
//
//        this.printVulkanError("Post startup");
//        this.overlay = new InGame(this);
//        if (this.serverIp != null) {
//            this.openScreen(new Connecting(this, this.serverIp, this.serverPort));
//        } else {
//            this.openScreen(new MainMenu());
//        }
//    }
//
//    private void printVulkanError(String string) {
//        int var2 = GL11.glGetError();
//        if (var2 != 0) {
//            String var3 = GLU.gluErrorString(var2);
//            System.out.println("########## GL ERROR ##########");
//            System.out.println("@ " + string);
//            System.out.println(var2 + ": " + var3);
//        }
//
//    }
//
//    public void run() {
//        this.running = true;
//
//        try {
//            this.init();
//        } catch (Exception var17) {
//            var17.printStackTrace();
//            this.onGameStartupFailure(new GameStartupError("Failed to start game", var17));
//            return;
//        }
//
//        try {
//            long var1 = System.currentTimeMillis();
//            int var3 = 0;
//
//            while(this.running) {
//                try {
//                    if (this.mcApplet != null && !this.mcApplet.isActive()) {
//                        break;
//                    }
//
//                    Box.method_85();
//                    Vec3f.method_1292();
//                    if (this.canvas == null && Display.isCloseRequested()) {
//                        this.scheduleStop();
//                    }
//
//                    if (this.paused && this.level != null) {
//                        float var4 = this.tickTimer.field_2370;
//                        this.tickTimer.method_1853();
//                        this.tickTimer.field_2370 = var4;
//                    } else {
//                        this.tickTimer.method_1853();
//                    }
//
//                    long var23 = System.nanoTime();
//
//                    for(int var6 = 0; var6 < this.tickTimer.field_2369; ++var6) {
//                        ++this.ticksPlayed;
//
//                        try {
//                            this.tick();
//                        } catch (SessionLockException var16) {
//                            this.level = null;
//                            this.setLevel((Level)null);
//                            this.openScreen(new LevelSaveConflict());
//                        }
//                    }
//
//                    long var24 = System.nanoTime() - var23;
//                    this.printOpenGLError("Pre render");
//                    BlockRenderer.fancyGraphics = this.options.fancyGraphics;
//                    this.soundHelper.setSoundPosition(this.player, this.tickTimer.field_2370);
//                    GL11.glEnable(3553);
//                    if (this.level != null) {
//                        this.level.method_232();
//                    }
//
//                    if (!Keyboard.isKeyDown(65)) {
//                        Display.update();
//                    }
//
//                    if (this.player != null && this.player.isInsideWall()) {
//                        this.options.thirdPerson = false;
//                    }
//
//                    if (!this.skipGameRender) {
//                        if (this.interactionManager != null) {
//                            this.interactionManager.method_1706(this.tickTimer.field_2370);
//                        }
//
//                        this.gameRenderer.method_1844(this.tickTimer.field_2370);
//                    }
//
//                    if (!Display.isActive()) {
//                        if (this.isFullscreen) {
//                            this.toggleFullscreen();
//                        }
//
//                        Thread.sleep(10L);
//                    }
//
//                    if (this.options.debugHud) {
//                        this.method_2111(var24);
//                    } else {
//                        this.lastFrameRenderTime = System.nanoTime();
//                    }
//
//                    this.achievement.renderBannerAndLicenseText();
//                    Thread.yield();
//                    if (Keyboard.isKeyDown(65)) {
//                        Display.update();
//                    }
//
//                    this.checkTakingScreenshot();
//                    if (this.canvas != null && !this.isFullscreen && (this.canvas.getWidth() != this.actualWidth || this.canvas.getHeight() != this.actualHeight)) {
//                        this.actualWidth = this.canvas.getWidth();
//                        this.actualHeight = this.canvas.getHeight();
//                        if (this.actualWidth <= 0) {
//                            this.actualWidth = 1;
//                        }
//
//                        if (this.actualHeight <= 0) {
//                            this.actualHeight = 1;
//                        }
//
//                        this.updateScreenResolution(this.actualWidth, this.actualHeight);
//                    }
//
//                    this.printOpenGLError("Post render");
//                    ++var3;
//
//                    for(this.paused = !this.hasLevel() && this.currentScreen != null && this.currentScreen.isPauseScreen(); System.currentTimeMillis() >= var1 + 1000L; var3 = 0) {
//                        this.fpsDebugString = var3 + " fps, " + class_66.field_230 + " chunk updates";
//                        class_66.field_230 = 0;
//                        var1 += 1000L;
//                    }
//                } catch (SessionLockException var18) {
//                    this.level = null;
//                    this.setLevel((Level)null);
//                    this.openScreen(new LevelSaveConflict());
//                } catch (OutOfMemoryError var19) {
//                    this.method_2131();
//                    this.openScreen(new OutOfMemory());
//                    System.gc();
//                }
//            }
//        } catch (ProgressListenerError var20) {
//        } catch (Throwable var21) {
//            this.method_2131();
//            var21.printStackTrace();
//            this.onGameStartupFailure(new GameStartupError("Unexpected error", var21));
//        } finally {
//            this.stop();
//        }
//
//    }
//
//    @Override
//    public void showGameStartupError(GameStartupError arg) {
//        mcApplet.removeAll();
//        mcApplet.setLayout(new BorderLayout());
//        mcApplet.add(new GameStartupErrorPanel(arg), "Center");
//        mcApplet.validate();
//    }
//
//    @Environment(EnvType.CLIENT)
//    public static class AppletCanvas extends Canvas {
//        private final MinecraftApplet applet;
//
//        public AppletCanvas(MinecraftApplet applet) {
//            this.applet = applet;
//        }
//        public synchronized void addNotify() {
//            super.addNotify();
//            this.applet.onAddNotify();
//        }
//
//        public synchronized void removeNotify() {
//            this.applet.onRemoveNotify();
//            super.removeNotify();
//        }
//    }
//}