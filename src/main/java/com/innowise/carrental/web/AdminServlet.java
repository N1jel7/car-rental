package com.innowise.carrental.web;

import com.innowise.carrental.entity.Booking;
import com.innowise.carrental.entity.BookingStatus;
import com.innowise.carrental.entity.Car;
import com.innowise.carrental.entity.CarImage;
import com.innowise.carrental.exception.ServiceException;
import com.innowise.carrental.exception.ValidationException;
import com.innowise.carrental.service.BookingService;
import com.innowise.carrental.service.CarService;
import com.innowise.carrental.util.FileUploadUtil;
import com.innowise.carrental.util.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminServlet.class);
    private static final String ADMIN_CARS_PAGE = "admin/cars";
    private static final String ADMIN_BOOKINGS_PAGE = "admin/bookings";
    private static final String ADMIN_CAR_FORM_PAGE = "admin/car-form";
    private static final String NEW_PREFIX = "new:";
    private static final String EXISTING_PREFIX = "existing:";
    private static final int PAGE_SIZE = 10;
    private static final int MAX_IMAGES = 10;
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private CarService carService;
    private BookingService bookingService;

    @Override
    public void init() {
        carService = new CarService();
        bookingService = new BookingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/cars" -> showCars(request, response);
            case "/cars/edit" -> showCarEdit(request, response);
            case "/cars/new" -> showCarForm(request, response, null);
            case "/bookings" -> showBookings(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "/";
        }

        switch (pathInfo) {
            case "/cars/create" -> handleCarCreate(request, response);
            case "/cars/update" -> handleCarUpdate(request, response);
            case "/cars/delete" -> handleCarDelete(request, response);
            case "/bookings/confirm" -> handleBookingConfirm(request, response);
            case "/bookings/complete" -> handleBookingComplete(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // --- GET handlers

    private void showCars(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = ServletUtil.parsePageParam(request.getParameter("page"));

        try {
            List<Car> cars = carService.findAvailable(page, PAGE_SIZE);
            int total = carService.countAvailable();
            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("cars", cars);
            context.setVariable("currentPage", page);
            context.setVariable("totalPages", totalPages);
            ServletUtil.render(ADMIN_CARS_PAGE, context, response, getServletContext());

        } catch (ServiceException e) {
            log.error("Admin failed to load cars", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void showCarEdit(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String carIdParam = request.getParameter("carId");

        try {
            long carId = Long.parseLong(carIdParam);
            Car car = carService.findById(carId);
            showCarForm(request, response, car);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (ServiceException e) {
            log.error("Admin failed to load car for edit id={}", carIdParam, e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Car not found");
        }
    }

    private void showCarForm(HttpServletRequest request, HttpServletResponse response,
                             Car car) throws ServletException, IOException {

        WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
        context.setVariable("car", car);

        if (car != null) {
            try {
                List<CarImage> images = carService.findImages(car.getId());
                context.setVariable("images", images);
                context.setVariable("maxImages", MAX_IMAGES);
            } catch (ServiceException e) {
                log.error("Admin failed to load images for carId={}", car.getId(), e);
                context.setVariable("images", List.of());
                context.setVariable("maxImages", MAX_IMAGES);
            }
        }

        ServletUtil.render(ADMIN_CAR_FORM_PAGE, context, response, getServletContext());
    }

    private void showBookings(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int page = ServletUtil.parsePageParam(request.getParameter("page"));
        String statusParam = request.getParameter("status");

        try {
            List<Booking> bookings;
            int total;

            if (statusParam != null && !statusParam.isBlank()) {

                BookingStatus status = BookingStatus.valueOf(statusParam);
                bookings = bookingService.findByStatus(status, page, PAGE_SIZE);
                total = bookingService.countByStatus(status);

            } else {

                bookings = bookingService.findByStatus(BookingStatus.PENDING, page, PAGE_SIZE);
                total = bookingService.countByStatus(BookingStatus.PENDING);

            }

            int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);

            WebContext context = ServletUtil.buildWebContext(request, response, getServletContext());
            context.setVariable("bookings", bookings);
            context.setVariable("currentPage", page);
            context.setVariable("totalPages", totalPages);
            context.setVariable("selectedStatus", statusParam);
            ServletUtil.render(ADMIN_BOOKINGS_PAGE, context, response, getServletContext());

        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/admin/bookings");
        } catch (ServiceException e) {
            log.error("Admin failed to load bookings", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // --- POST handlers

    private void handleCarCreate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            List<Part> imageParts = collectImageParts(request);

            if (imageParts.isEmpty()) {
                redirect(request, response, "/admin/cars/new", "error", "At least one photo is required");
                return;
            }
            if (imageParts.size() > MAX_IMAGES) {
                redirect(request, response, "/admin/cars/new", "error", "Too many photos: max " + MAX_IMAGES);
                return;
            }
            String imageError = validateImageParts(imageParts);
            if (imageError != null) {
                redirect(request, response, "/admin/cars/new", "error", imageError);
                return;
            }

            Car car = carService.add(
                    request.getParameter("make"),
                    request.getParameter("model"),
                    Integer.parseInt(request.getParameter("year")),
                    new BigDecimal(request.getParameter("pricePerDay")),
                    request.getParameter("description")
            );

            int coverIndex = parseNewCoverIndex(request.getParameter("primarySelection"), imageParts.size());
            saveImages(car.getId(), imageParts, coverIndex);

            redirect(request, response, "/admin/cars", "success", "Car created successfully");

        } catch (ValidationException e) {
            redirect(request, response, "/admin/cars/new", "error", e.getMessage());
        } catch (NumberFormatException e) {
            redirect(request, response, "/admin/cars/new", "error", "Please enter valid numbers for year and price");
        } catch (ServiceException | IOException e) {
            log.error("Admin failed to create car", e);
            redirect(request, response, "/admin/cars", "error", "Failed to create the car. Please try again");
        }
    }

    private void handleCarUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String carIdParam = request.getParameter("carId");

        try {
            long carId = Long.parseLong(carIdParam);
            carService.update(
                    carId,
                    request.getParameter("make"),
                    request.getParameter("model"),
                    Integer.parseInt(request.getParameter("year")),
                    new BigDecimal(request.getParameter("pricePerDay")),
                    request.getParameter("description")
            );

            Set<Long> deletedImageIds = deleteRequestedImages(request);

            List<Part> imageParts = collectImageParts(request);
            int existingCount = carService.findImages(carId).size();
            String editPath = "/admin/cars/edit?carId=" + carId;

            if (existingCount + imageParts.size() > MAX_IMAGES) {
                redirect(request, response, editPath, "error", "Too many photos: max " + MAX_IMAGES);
                return;
            }
            String imageError = validateImageParts(imageParts);
            if (imageError != null) {
                redirect(request, response, editPath, "error", imageError);
                return;
            }

            // "primarySelection" is either "existing:<imageId>" (an already-saved photo)
            // or "new:<index>" (one of the files being uploaded right now).
            String primarySelection = request.getParameter("primarySelection");
            Integer newCoverIndex = parseNewIndex(primarySelection);
            saveImages(carId, imageParts, newCoverIndex != null ? newCoverIndex : -1);

            Long existingCoverId = parseExistingId(primarySelection);
            if (existingCoverId != null && !deletedImageIds.contains(existingCoverId)) {
                carService.setPrimaryImage(carId, existingCoverId);
            }

            // If no image ended up primary (e.g. the chosen/primary photo was deleted),
            // fall back to making the first remaining photo primary.
            List<CarImage> remainingImages = carService.findImages(carId);
            boolean hasPrimary = remainingImages.stream().anyMatch(CarImage::isPrimary);
            if (!hasPrimary && !remainingImages.isEmpty()) {
                carService.setPrimaryImage(carId, remainingImages.get(0).getId());
            }

            log.info("Admin updated car id={}", carId);
            redirect(request, response, "/admin/cars", "success", "Car updated successfully");

        } catch (ValidationException e) {
            redirect(request, response, "/admin/cars", "error", e.getMessage());
        } catch (NumberFormatException e) {
            redirect(request, response, "/admin/cars", "error", "Please enter valid numbers for year and price");
        } catch (ServiceException e) {
            log.error("Admin failed to update car id={}", carIdParam, e);
            redirect(request, response, "/admin/cars", "error", "Failed to update the car. Please try again");
        }
    }

    private void handleCarDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String carIdParam = request.getParameter("carId");

        try {
            long carId = Long.parseLong(carIdParam);
            carService.delete(carId);

            log.info("Admin deleted car id={}", carId);
            redirect(request, response, "/admin/cars", "success", "Car deleted successfully");

        } catch (NumberFormatException e) {
            redirect(request, response, "/admin/cars", "error", "Invalid car");
        } catch (ServiceException e) {
            log.error("Admin failed to delete car id={}", carIdParam, e);
            redirect(request, response, "/admin/cars", "error", e.getMessage());
        }
    }

    private void handleBookingConfirm(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String bookingIdParam = request.getParameter("id");

        try {
            long bookingId = Long.parseLong(bookingIdParam);
            bookingService.confirm(bookingId);

            log.info("Admin confirmed booking id={}", bookingId);
            redirect(request, response, "/admin/bookings", "success", "Booking confirmed");

        } catch (NumberFormatException e) {
            redirect(request, response, "/admin/bookings", "error", "Invalid booking");
        } catch (ServiceException e) {
            log.error("Admin failed to confirm booking id={}", bookingIdParam, e);
            redirect(request, response, "/admin/bookings", "error", e.getMessage());
        }
    }

    private void handleBookingComplete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String bookingIdParam = request.getParameter("id");

        try {
            long bookingId = Long.parseLong(bookingIdParam);
            bookingService.complete(bookingId);

            log.info("Admin completed booking id={}", bookingId);
            redirect(request, response, "/admin/bookings", "success", "Booking completed");

        } catch (NumberFormatException e) {
            redirect(request, response, "/admin/bookings", "error", "Invalid booking");
        } catch (ServiceException e) {
            log.error("Admin failed to complete booking id={}", bookingIdParam, e);
            redirect(request, response, "/admin/bookings", "error", e.getMessage());
        }
    }

    // --- Shared helpers

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          String path, String param, String message) throws IOException {
        String separator = path.contains("?") ? "&" : "?";
        response.sendRedirect(request.getContextPath() + path + separator + param + "="
                + ServletUtil.encode(message));
    }

    private List<Part> collectImageParts(HttpServletRequest request) throws IOException, ServletException {
        List<Part> parts = new ArrayList<>();
        for (Part part : request.getParts()) {
            if ("images".equals(part.getName()) && part.getSize() > 0) {
                parts.add(part);
            }
        }
        return parts;
    }

    // Returns a readable error message for the first invalid part or null if ok.
    private String validateImageParts(List<Part> parts) {
        for (Part part : parts) {
            if (part.getSize() > MAX_IMAGE_SIZE) {
                return "File too large: max 10 MB";
            }
            String extension = extractExtension(part.getSubmittedFileName());
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                return "Invalid file type. Allowed: jpg, jpeg, png, webp";
            }
        }
        return null;
    }

    // Saves every part to disk and links it to the car
    private void saveImages(long carId, List<Part> parts, int coverIndex) throws IOException, ServiceException {
        String uploadsRoot = FileUploadUtil.getUploadsRoot();
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            String path = FileUploadUtil.save(
                    part.getInputStream(),
                    part.getSubmittedFileName(),
                    "cars",
                    uploadsRoot
            );
            carService.addImage(carId, path, i == coverIndex);
        }
    }

    private Set<Long> deleteRequestedImages(HttpServletRequest request) throws ServiceException {
        Set<Long> deletedImageIds = new HashSet<>();
        String[] deleteIds = request.getParameterValues("deleteImages");
        if (deleteIds == null) {
            return deletedImageIds;
        }
        for (String deleteId : deleteIds) {
            Long id = parseLongOrNull(deleteId);
            if (id != null) {
                carService.deleteImage(id);
                deletedImageIds.add(id);
            }
        }
        return deletedImageIds;
    }

    private String extractExtension(String filename) {
        return filename != null && filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
                : "";
    }

    // Reads the "primarySelection" value (example "new:2"), default 0 when nothing valid was picked.
    private int parseNewCoverIndex(String primarySelection, int uploadedCount) {
        Integer index = parseNewIndex(primarySelection);
        return (index != null && index >= 0 && index < uploadedCount) ? index : 0;
    }

    private Integer parseNewIndex(String primarySelection) {
        if (primarySelection == null || !primarySelection.startsWith(NEW_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(primarySelection.substring(NEW_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseExistingId(String primarySelection) {
        if (primarySelection == null || !primarySelection.startsWith(EXISTING_PREFIX)) {
            return null;
        }
        return parseLongOrNull(primarySelection.substring(EXISTING_PREFIX.length()));
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
