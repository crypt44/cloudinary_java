package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Coordinates;
import com.cloudinary.Transformation;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.utils.ObjectUtils;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

@SuppressWarnings({"rawtypes", "unchecked", "JavaDoc"})
abstract public class AbstractApiTest extends MockableTest {
    private static final String API_TEST = "api_test_" + SUFFIX;
    private static final String API_TEST_1 = API_TEST + "_1";
    private static final String API_TEST_2 = API_TEST + "_2";
    private static final String API_TEST_3 = API_TEST + "_3";
    private static final String API_TEST_5 = API_TEST + "_5";
    public static final String API_TEST_TRANSFORMATION = "api_test_transformation_" + SUFFIX;
    public static final String API_TEST_TRANSFORMATION_2 = API_TEST_TRANSFORMATION + "2";
    public static final String API_TEST_TRANSFORMATION_3 = API_TEST_TRANSFORMATION + "3";
    public static final String API_TEST_UPLOAD_PRESET = "api_test_upload_preset_" + SUFFIX;
    public static final String API_TEST_UPLOAD_PRESET_2 = API_TEST_UPLOAD_PRESET + "2";
    public static final String API_TEST_UPLOAD_PRESET_3 = API_TEST_UPLOAD_PRESET + "3";
    public static final String API_TEST_UPLOAD_PRESET_4 = API_TEST_UPLOAD_PRESET + "4";
    protected Api api;

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
            return;
        }
        Map options = ObjectUtils.asMap("public_id", API_TEST, "tags", new String[]{SDK_TEST_TAG, uniqueTag}, "context", "key=value", "eager",
                Collections.singletonList(new Transformation().width(100).crop("scale")));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
        options.put("public_id", API_TEST_1);
        cloudinary.uploader().upload(SRC_TEST_IMAGE, options);
    }

    @AfterClass
    public static void tearDownClass() {
        Api api  = MockableTest.cleanUp();
        try {
            api.deleteResources(Arrays.asList(API_TEST, API_TEST_1, API_TEST_2, API_TEST_3, API_TEST_5), ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteTransformation(API_TEST_TRANSFORMATION, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteTransformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteTransformation(API_TEST_TRANSFORMATION_3, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_2, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_3, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }
        try {
            api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
        }

    }
    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
        this.api = cloudinary.api();


    }

    public Map findByAttr(List<Map> elements, String attr, Object value) {
        for (Map element : elements) {
            if (value.equals(element.get(attr))) {
                return element;
            }
        }
        return null;
    }

    @Test
    public void test01ResourceTypes() throws Exception {
        // should allow listing resource_types
        Map result = api.resourceTypes(ObjectUtils.emptyMap());
        final List<String> resource_types = (List<String>) result.get("resource_types");
        assertThat(resource_types, hasItem("image"));
    }

    @Test
    public void test02Resources() throws Exception {
        // should allow listing resources
        Map resource = preloadResource();

        final List<Map> resources = new ArrayList<Map>();
        String next_cursor = null;
        do {
            Map result = api.resources(ObjectUtils.asMap("max_results", 500, "next_cursor", next_cursor));
            resources.addAll((List) result.get("resources"));
            next_cursor = (String) result.get("next_cursor");
        } while (next_cursor != null );
        assertThat(resources, hasItem(allOf(hasEntry("public_id", (String) resource.get("public_id")),hasEntry("type", "upload"))));
    }

    @Test
    public void test03ResourcesCursor() throws Exception {
        // should allow listing resources with cursor
        Map options = new HashMap();
        options.put("max_results", 1);
        Map result = api.resources(options);
        List<Map> resources = (List<Map>) result.get("resources");
        assertNotNull(resources);
        assertEquals(1, resources.size());
        assertNotNull(result.get("next_cursor"));

        options.put("next_cursor", result.get("next_cursor"));
        Map result2 = api.resources(options);
        List<Map> resources2 = (List<Map>) result2.get("resources");
        assertNotNull(resources2);
        assertEquals(resources2.size(), 1);
        assertNotSame(resources2.get(0).get("public_id"), resources.get(0).get("public_id"));
    }

    @Test
    public void test04ResourcesByType() throws Exception {
        // should allow listing resources by type
        Map resource = preloadResource();
        Map result = api.resources(ObjectUtils.asMap("type", "upload"));
        List<Map> resources = (List) result.get("resources");
        assertThat(resources, hasItem(hasEntry("public_id", (String) resource.get("public_id"))));
    }

    @Test
    public void test05ResourcesByPrefix() throws Exception {
        // should allow listing resources by prefix
        Map result = api.resources(ObjectUtils.asMap("type", "upload", "prefix", API_TEST, "tags", true, "context", true));
        List<Map> resources = (List) result.get("resources");
        assertThat(resources, hasItem(hasEntry("public_id", (Object) API_TEST)));
        assertThat(resources, hasItem(hasEntry("public_id", (Object) API_TEST_1)));
//        resources = (List<Map<? extends String, ?>>) result.get("resources");
        assertThat(resources, hasItem(allOf(hasEntry("public_id", API_TEST),hasEntry("type", "upload"))));
        assertThat(resources, hasItem(hasEntry("context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value")))));
        assertThat(resources, hasItem(hasEntry(equalTo("tags"),  hasItem( SDK_TEST_TAG))));
    }

    @Test
    public void testResourcesListingDirection() throws Exception {
        // should allow listing resources in both directions
        Map result = api.resourcesByTag(uniqueTag, ObjectUtils.asMap("type", "upload", "direction", "asc"));
        List<Map> resources = (List<Map>) result.get("resources");
        result = api.resourcesByTag(uniqueTag, ObjectUtils.asMap("type", "upload", "direction", -1));
        List<Map> resourcesDesc = (List<Map>) result.get("resources");
        Collections.reverse(resources);
        assertEquals(resources, resourcesDesc);
    }

    @Ignore
    public void testResourcesListingStartAt() throws Exception {
        // should allow listing resources by start date - make sure your clock
        // is set correctly!!!
        Thread.sleep(2000L);
        java.util.Date startAt = new java.util.Date();
        Thread.sleep(2000L);
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", SDK_TEST_TAG));
        ApiResponse listResources = api.resources(ObjectUtils.asMap("type", "upload", "start_at", startAt, "direction", "asc"));
        List<Map> resources = (List<Map>) listResources.get("resources");
        assertEquals(response.get("public_id"), resources.get(0).get("public_id"));
    }

    @Test
    public void testResourcesByPublicIds() throws Exception {
        // should allow listing resources by public ids
        Map result = api.resourcesByIds(Arrays.asList(API_TEST, API_TEST_1, "bogus"), ObjectUtils.asMap("type", "upload", "tags", true, "context", true));
        List<Map> resources = (List<Map>) result.get("resources");
        assertEquals(2, resources.size());
        assertNotNull(findByAttr(resources, "public_id", API_TEST));
        assertNotNull(findByAttr(resources, "public_id", API_TEST_1));
        assertNotNull(findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value"))));
        boolean found = false;
        for (Map r : resources) {
            ArrayList tags = (ArrayList) r.get("tags");
            found = found || tags.contains(SDK_TEST_TAG);
        }
        assertTrue(found);
    }

    @Test
    public void test06ResourcesTag() throws Exception {
        // should allow listing resources by tag
        Map result = api.resourcesByTag(SDK_TEST_TAG, ObjectUtils.asMap("tags", true, "context", true));
        Map resource = findByAttr((List<Map>) result.get("resources"), "public_id", API_TEST);
        assertNotNull(resource);
        resource = findByAttr((List<Map>) result.get("resources"), "context", ObjectUtils.asMap("custom", ObjectUtils.asMap("key", "value")));
        assertNotNull(resource);
        List<Map> resources = (List<Map>) result.get("resources");
        boolean found = false;
        for (Map r : resources) {
            ArrayList tags = (ArrayList) r.get("tags");
            found = found || tags.contains(SDK_TEST_TAG);
        }
        assertTrue(found);
    }

    @Test
    public void test07ResourceMetadata() throws Exception {
        // should allow get resource metadata
        Map resource = api.resource(API_TEST, ObjectUtils.emptyMap());
        assertNotNull(resource);
        assertEquals(resource.get("public_id"), API_TEST);
        assertEquals(resource.get("bytes"), 3381);
        assertEquals(((List) resource.get("derived")).size(), 1);
    }

    @Test
    public void test08DeleteDerived() throws Exception {
        // should allow deleting derived resource
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", API_TEST_3, "tags", SDK_TEST_TAG, "eager", Collections.singletonList(new Transformation().width(101).crop("scale"))));
        Map resource = api.resource(API_TEST_3, ObjectUtils.emptyMap());
        assertNotNull(resource);
        List<Map> derived = (List<Map>) resource.get("derived");
        assertEquals(derived.size(), 1);
        String derived_resource_id = (String) derived.get(0).get("id");
        api.deleteDerivedResources(Collections.singletonList(derived_resource_id), ObjectUtils.emptyMap());
        resource = api.resource(API_TEST_3, ObjectUtils.emptyMap());
        assertNotNull(resource);
        derived = (List<Map>) resource.get("derived");
        assertEquals(derived.size(), 0);
    }

    @Test(expected = NotFound.class)
    public void test09DeleteResources() throws Exception {
        // should allow deleting resources
        String public_id = "api_,test3";
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", public_id, "tags", SDK_TEST_TAG));
        Map resource = api.resource(public_id, ObjectUtils.emptyMap());
        assertNotNull(resource);
        api.deleteResources(Arrays.asList(API_TEST_2, public_id), ObjectUtils.emptyMap());
        api.resource(public_id, ObjectUtils.emptyMap());
    }

    @Test(expected = NotFound.class)
    public void test09aDeleteResourcesByPrefix() throws Exception {
        // should allow deleting resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "api_test_by_prefix", "tags", SDK_TEST_TAG));
        Map resource = api.resource("api_test_by_prefix", ObjectUtils.emptyMap());
        assertNotNull(resource);
        api.deleteResourcesByPrefix("api_test_by", ObjectUtils.emptyMap());
        api.resource("api_test_by_prefix", ObjectUtils.emptyMap());
    }

    @Test(expected = NotFound.class)
    public void test09aDeleteResourcesByTags() throws Exception {
        // should allow deleting resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", API_TEST + "_4", "tags", Collections.singletonList("api_test_tag_for_delete")));
        Map resource = api.resource(API_TEST + "_4", ObjectUtils.emptyMap());
        assertNotNull(resource);
        api.deleteResourcesByTag("api_test_tag_for_delete", ObjectUtils.emptyMap());
        api.resource(API_TEST + "_4", ObjectUtils.emptyMap());
    }

    @Test
    public void test10Tags() throws Exception {
        // should allow listing tags
        Map result = api.tags(ObjectUtils.asMap("max_results", 500));
        List<String> tags = (List<String>) result.get("tags");
        assertThat( tags, hasItem(SDK_TEST_TAG));
    }

    @Test
    public void test11TagsPrefix() throws Exception {
        // should allow listing tag by prefix
        Map result = api.tags(ObjectUtils.asMap("prefix", SDK_TEST_TAG.substring(0,SDK_TEST_TAG.length()-1)));
        List<String> tags = (List<String>) result.get("tags");
        assertThat( tags, hasItem(SDK_TEST_TAG));
        result = api.tags(ObjectUtils.asMap("prefix", "api_test_no_such_tag"));
        tags = (List<String>) result.get("tags");
        assertEquals(0, tags.size());
    }

    @Test
    public void test12Transformations() throws Exception {
        // should allow listing transformations
        Map result = api.transformations(ObjectUtils.emptyMap());
        Map transformation = findByAttr((List<Map>) result.get("transformations"), "name", "c_scale,w_100");

        assertNotNull(transformation);
        assertTrue((Boolean) transformation.get("used"));
    }

    @Test
    public void test13TransformationMetadata() throws Exception {
        // should allow getting transformation metadata
        final Transformation tr = new Transformation().crop("scale").width(100);
        preloadResource(ObjectUtils.asMap("eager", Collections.singletonList(tr)));
        Map transformation = api.transformation("c_scale,w_100", ObjectUtils.asMap("max_results", 500));
        assertNotNull(transformation);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), tr.generate());
    }

    @Test
    public void test14TransformationUpdate() throws Exception {
        // should allow updating transformation allowed_for_strict
        api.updateTransformation("c_scale,w_100", ObjectUtils.asMap("allowed_for_strict", true), ObjectUtils.emptyMap());
        Map transformation = api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), true);
        api.updateTransformation("c_scale,w_100", ObjectUtils.asMap("allowed_for_strict", false), ObjectUtils.emptyMap());
        transformation = api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), false);
    }

    @Test
    public void test15TransformationCreate() throws Exception {
        // should allow creating named transformation
        api.createTransformation(API_TEST_TRANSFORMATION, new Transformation().crop("scale").width(102).generate(), ObjectUtils.emptyMap());
        Map transformation = api.transformation(API_TEST_TRANSFORMATION, ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(transformation.get("allowed_for_strict"), true);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(102).generate());
        assertEquals(transformation.get("used"), false);
    }

    @Test
    public void test15aTransformationUnsafeUpdate() throws Exception {
        // should allow unsafe update of named transformation
        api.createTransformation(API_TEST_TRANSFORMATION_3, new Transformation().crop("scale").width(102).generate(), ObjectUtils.emptyMap());
        api.updateTransformation(API_TEST_TRANSFORMATION_3, ObjectUtils.asMap("unsafe_update", new Transformation().crop("scale").width(103).generate()),
                ObjectUtils.emptyMap());
        Map transformation = api.transformation(API_TEST_TRANSFORMATION_3, ObjectUtils.emptyMap());
        assertNotNull(transformation);
        assertEquals(new Transformation((List<Map>) transformation.get("info")).generate(), new Transformation().crop("scale").width(103).generate());
        assertEquals(transformation.get("used"), false);
    }

    @Test
    public void test16aTransformationDelete() throws Exception {
        // should allow deleting named transformation
        api.createTransformation(API_TEST_TRANSFORMATION_2, new Transformation().crop("scale").width(103).generate(), ObjectUtils.emptyMap());
        api.transformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
        api.deleteTransformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
    }

    @Test(expected = NotFound.class)
    public void test16bTransformationDelete() throws Exception {
        api.transformation(API_TEST_TRANSFORMATION_2, ObjectUtils.emptyMap());
    }

    @Test
    public void test17aTransformationDeleteImplicit() throws Exception {
        // should allow deleting implicit transformation
        api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
        api.deleteTransformation("c_scale,w_100", ObjectUtils.emptyMap());
    }

    /**
     * @throws Exception
     * @expectedException \Cloudinary\Api\NotFound
     */
    @Test(expected = NotFound.class)
    public void test17bTransformationDeleteImplicit() throws Exception {
        api.transformation("c_scale,w_100", ObjectUtils.emptyMap());
    }

    @Test
    public void test18Usage() throws Exception {
        // should support usage API call
        Map result = api.usage(ObjectUtils.emptyMap());
        assertNotNull(result.get("last_updated"));
    }

    @Test
    public void test19Ping() throws Exception {
        // should support ping API call
        Map result = api.ping(ObjectUtils.emptyMap());
        assertEquals(result.get("status"), "ok");
    }

    // This test must be last because it deletes (potentially) all dependent
    // transformations which some tests rely on.
    // Add @Test if you really want to test it - This test deletes derived
    // resources!
    public void testDeleteAllResources() throws Exception {
        // should allow deleting all resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", API_TEST_5, "tags", SDK_TEST_TAG, "eager", Collections.singletonList(new Transformation().crop("scale").width(2.0))));
        Map result = api.resource(API_TEST_5, ObjectUtils.emptyMap());
        assertEquals(1, ((org.cloudinary.json.JSONArray) result.get("derived")).length());
        api.deleteAllResources(ObjectUtils.asMap("keep_original", true));
        result = api.resource(API_TEST_5, ObjectUtils.emptyMap());
        // assertEquals(0, ((org.cloudinary.json.JSONArray)
        // result.get("derived")).size());
    }

    @Test
    public void testManualModeration() throws Exception {
        // should support setting manual moderation status
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", SDK_TEST_TAG));
        Map apiResult = api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("moderation_status", "approved", "tags", SDK_TEST_TAG));
        assertEquals("approved", ((Map) ((List<Map>) apiResult.get("moderation")).get(0)).get("status"));
    }

    @Test
    public void testOcrUpdate() {
        // should support requesting ocr info
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap( "tags", SDK_TEST_TAG));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("ocr", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testRawConvertUpdate() {
        // should support requesting raw conversion
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap( "tags", SDK_TEST_TAG));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("raw_convert", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testCategorizationUpdate() {
        // should support requesting categorization
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap( "tags", SDK_TEST_TAG));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("categorization", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testDetectionUpdate() {
        // should support requesting detection
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap( "tags", SDK_TEST_TAG));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("detection", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testSimilaritySearchUpdate() {
        // should support requesting similarity search
        try {
            Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap( "tags", SDK_TEST_TAG));
            api.update((String) uploadResult.get("public_id"), ObjectUtils.asMap("similarity_search", "illegal"));
        } catch (Exception e) {
            assertTrue(e instanceof BadRequest);
            assertTrue(e.getMessage().matches("^Illegal value(.*)"));
        }
    }

    @Test
    public void testUpdateCustomCoordinates() throws IOException, Exception {
        // should update custom coordinates
        Coordinates coordinates = new Coordinates("121,31,110,151");
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap( "tags", SDK_TEST_TAG));
        cloudinary.api().update(uploadResult.get("public_id").toString(), ObjectUtils.asMap("custom_coordinates", coordinates));
        Map result = cloudinary.api().resource(uploadResult.get("public_id").toString(), ObjectUtils.asMap("coordinates", true));
        int[] expected = new int[]{121, 31, 110, 151};
        ArrayList actual = (ArrayList) ((ArrayList) ((Map) result.get("coordinates")).get("custom")).get(0);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual.get(i));
        }
    }

    @Test
    public void testListUploadPresets() throws Exception {
        // should allow creating and listing upload_presets
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET, "folder", "folder"));
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET_2, "folder", "folder2"));
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET_3, "folder", "folder3"));

        ArrayList presets = (ArrayList) (api.uploadPresets(ObjectUtils.emptyMap()).get("presets"));
        assertEquals(((Map) presets.get(0)).get("name"), API_TEST_UPLOAD_PRESET_3);
        assertEquals(((Map) presets.get(1)).get("name"), API_TEST_UPLOAD_PRESET_2);
        assertEquals(((Map) presets.get(2)).get("name"), API_TEST_UPLOAD_PRESET);
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET, ObjectUtils.emptyMap());
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_2, ObjectUtils.emptyMap());
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_3, ObjectUtils.emptyMap());
    }

    @Test
    public void testGetUploadPreset() throws Exception {
        // should allow getting a single upload_preset
        String[] tags = {"a", "b", "c"};
        Map context = ObjectUtils.asMap("a", "b", "c", "d");
        Transformation transformation = new Transformation();
        transformation.width(100).crop("scale");
        Map result = api.createUploadPreset(ObjectUtils.asMap("unsigned", true, "folder", "folder", "transformation", transformation, "tags", tags, "context",
                context));
        String name = result.get("name").toString();
        Map preset = api.uploadPreset(name, ObjectUtils.emptyMap());
        assertEquals(preset.get("name"), name);
        assertEquals(Boolean.TRUE, preset.get("unsigned"));
        Map settings = (Map) preset.get("settings");
        assertEquals(settings.get("folder"), "folder");
        Map outTransformation = (Map) ((java.util.ArrayList) settings.get("transformation")).get(0);
        assertEquals(outTransformation.get("width"), 100);
        assertEquals(outTransformation.get("crop"), "scale");
        Object[] outTags = ((java.util.ArrayList) settings.get("tags")).toArray();
        assertArrayEquals(tags, outTags);
        Map outContext = (Map) settings.get("context");
        assertEquals(context, outContext);
    }

    @Test
    public void testDeleteUploadPreset() throws Exception {
        // should allow deleting upload_presets", :upload_preset => true do
        api.createUploadPreset(ObjectUtils.asMap("name", API_TEST_UPLOAD_PRESET_4, "folder", "folder"));
        api.uploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        api.deleteUploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        boolean error = false;
        try {
            api.uploadPreset(API_TEST_UPLOAD_PRESET_4, ObjectUtils.emptyMap());
        } catch (Exception e) {
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void testUpdateUploadPreset() throws Exception {
        // should allow updating upload_presets
        String name = api.createUploadPreset(ObjectUtils.asMap("folder", "folder")).get("name").toString();
        Map preset = api.uploadPreset(name, ObjectUtils.emptyMap());
        Map settings = (Map) preset.get("settings");
        settings.putAll(ObjectUtils.asMap("colors", true, "unsigned", true, "disallow_public_id", true));
        api.updateUploadPreset(name, settings);
        settings.remove("unsigned");
        preset = api.uploadPreset(name, ObjectUtils.emptyMap());
        assertEquals(name, preset.get("name"));
        assertEquals(Boolean.TRUE, preset.get("unsigned"));
        assertEquals(settings, preset.get("settings"));
        api.deleteUploadPreset(name, ObjectUtils.emptyMap());
    }

    @Test
    public void testListByModerationUpdate() throws Exception {
        // "should support listing by moderation kind and value
        List<Map> resources;

        Map result1 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", SDK_TEST_TAG));
        Map result2 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", SDK_TEST_TAG));
        Map result3 = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("moderation", "manual", "tags", SDK_TEST_TAG));
        api.update((String) result1.get("public_id"), ObjectUtils.asMap("moderation_status", "approved"));
        api.update((String) result2.get("public_id"), ObjectUtils.asMap("moderation_status", "rejected"));
        Map approved = api.resourcesByModeration("manual", "approved", ObjectUtils.asMap("max_results", 1000));
        Map rejected = api.resourcesByModeration("manual", "rejected", ObjectUtils.asMap("max_results", 1000));
        Map pending = api.resourcesByModeration("manual", "pending", ObjectUtils.asMap("max_results", 1000));

        resources = (List<Map>) approved.get("resources");
        assertThat(resources, hasItem(hasEntry("public_id", result1.get("public_id"))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result2.get("public_id")))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result3.get("public_id")))));

        resources = (List<Map>) rejected.get("resources");
        assertThat(resources, not(hasItem(hasEntry("public_id", result1.get("public_id")))));
        assertThat(resources, hasItem(hasEntry("public_id", result2.get("public_id"))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result3.get("public_id")))));

        resources = (List<Map>) pending.get("resources");
        assertThat(resources, not(hasItem(hasEntry("public_id", result1.get("public_id")))));
        assertThat(resources, not(hasItem(hasEntry("public_id", result2.get("public_id")))));
        assertThat(resources, hasItem(hasEntry("public_id", result3.get("public_id"))));
    }

    // For this test to work, "Auto-create folders" should be enabled in the
    // Upload Settings.
    // Uncomment @Test if you really want to test it.
    // @Test
    public void testFolderApi() throws Exception {
        // should allow deleting all resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/item", "tags", SDK_TEST_TAG));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder2/item", "tags", SDK_TEST_TAG));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/test_subfolder1/item", "tags", SDK_TEST_TAG));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("public_id", "test_folder1/test_subfolder2/item", "tags", SDK_TEST_TAG));
        Map result = api.rootFolders(null);
        assertEquals("test_folder1", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(0)).get("name"));
        assertEquals("test_folder2", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(1)).get("name"));
        result = api.subFolders("test_folder1", null);
        assertEquals("test_folder1/test_subfolder1", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(0)).get("path"));
        assertEquals("test_folder1/test_subfolder2", ((Map) ((org.cloudinary.json.JSONArray) result.get("folders")).get(1)).get("path"));
        try {
            api.subFolders("test_folder", null);
        } catch (Exception e) {
            assertTrue(e instanceof NotFound);
        }
        api.deleteResourcesByPrefix("test_folder", ObjectUtils.emptyMap());
    }

    @Test
    public void testRestore() throws Exception {
        // should support restoring resources
        cloudinary.uploader().upload(SRC_TEST_IMAGE,
                ObjectUtils.asMap("public_id", "api_test_restore", "backup", true, "tags", SDK_TEST_TAG));
        Map resource = api.resource("api_test_restore", ObjectUtils.emptyMap());
        assertEquals(resource.get("bytes"), 3381);
        api.deleteResources(Collections.singletonList("api_test_restore"), ObjectUtils.emptyMap());
        resource = api.resource("api_test_restore", ObjectUtils.emptyMap());
        assertEquals(resource.get("bytes"), 0);
        assertTrue((Boolean) resource.get("placeholder"));
        Map response = api.restore(Collections.singletonList("api_test_restore"), ObjectUtils.emptyMap());
        Map info = (Map) response.get("api_test_restore");
        assertNotNull(info);
        assertEquals(info.get("bytes"), 3381);
        resource = api.resource("api_test_restore", ObjectUtils.emptyMap());
        assertEquals(resource.get("bytes"), 3381);
    }

    @Test
    public void testUploadMapping() throws Exception {
        try {
            api.deleteUploadMapping("api_test_upload_mapping", ObjectUtils.emptyMap());
        } catch (Exception ignored) {

        }
        api.createUploadMapping("api_test_upload_mapping", ObjectUtils.asMap("template", "http://cloudinary.com"));
        Map result = api.uploadMapping("api_test_upload_mapping", ObjectUtils.emptyMap());
        assertEquals(result.get("template"), "http://cloudinary.com");
        api.updateUploadMapping("api_test_upload_mapping", ObjectUtils.asMap("template", "http://res.cloudinary.com"));
        result = api.uploadMapping("api_test_upload_mapping", ObjectUtils.emptyMap());
        assertEquals(result.get("template"), "http://res.cloudinary.com");
        result = api.uploadMappings(ObjectUtils.emptyMap());
        ListIterator mappings = ((ArrayList) result.get("mappings")).listIterator();
        boolean found = false;
        while (mappings.hasNext()) {
            Map mapping = (Map) mappings.next();
            if (mapping.get("folder").equals("api_test_upload_mapping")
                    && mapping.get("template").equals("http://res.cloudinary.com")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        api.deleteUploadMapping("api_test_upload_mapping", ObjectUtils.emptyMap());
        result = api.uploadMappings(ObjectUtils.emptyMap());
        found = false;
        while (mappings.hasNext()) {
            Map mapping = (Map) mappings.next();
            if (mapping.get("folder").equals("api_test_upload_mapping")
                    && mapping.get("template").equals("http://res.cloudinary.com")) {
                found = true;
                break;
            }
        }
        assertTrue(!found);
    }

    @Test
    public void testPublishByIds() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", uniqueTag, "type", "authenticated"));
        String publicId = (String) response.get("public_id");
        response = cloudinary.api().publishByIds(Arrays.asList(publicId), null);
        List published = (List) response.get("published");
        assertNotNull(published);
        assertEquals(published.size(), 1);
        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testPublishByPrefix() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", uniqueTag, "type", "authenticated"));
        String publicId = (String) response.get("public_id");
        response = cloudinary.api().publishByPrefix(publicId.substring(0, publicId.length() - 2), null);
        List published = (List) response.get("published");
        assertNotNull(published);
        assertEquals(published.size(), 1);
        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }

    @Test
    public void testPublishByTag() throws Exception {
        Map response = cloudinary.uploader().upload(SRC_TEST_IMAGE, ObjectUtils.asMap("tags", Arrays.asList(uniqueTag, uniqueTag + "1"), "type", "authenticated"));
        String publicId = (String) response.get("public_id");
        response = cloudinary.api().publishByTag(uniqueTag + "1", null);
        List published = (List) response.get("published");
        assertNotNull(published);
        assertEquals(published.size(), 1);
        Map resource = (Map) published.get(0);
        assertEquals(resource.get("public_id"), publicId);
        assertNotNull(resource.get("url"));
        cloudinary.uploader().destroy(publicId, null);
    }
}
