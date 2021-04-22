import InternalSection, {SectionClass} from "./Section";
import InternalGallery, {GalleryClass} from "./Gallery/Gallery";
import GalleryItem, {GalleryItemClass} from "./Gallery/GalleryItem";
import SectionItem, {SectionItemClass} from "./SectionItem";
// import "./style/index.less";

/* Section */
type InternalSection = typeof InternalSection;

interface Section extends InternalSection {
    class: string;
    Item: typeof SectionItem;
    ItemClass: string;
}

const EMonitorSection: Section = InternalSection as Section;

EMonitorSection.Item = SectionItem;
EMonitorSection.class = SectionClass;
EMonitorSection.ItemClass = SectionItemClass;

/* Gallery */
type InternalGallery = typeof InternalGallery;

interface Gallery extends InternalGallery {
    class: string;
    Item: typeof GalleryItem;
    ItemClass: string;
}

const EMonitorGallery: Gallery = InternalGallery as Gallery;

EMonitorGallery.Item = GalleryItem;
EMonitorGallery.class = GalleryClass;
EMonitorGallery.ItemClass = GalleryItemClass;

export {
    EMonitorSection,
    EMonitorGallery,
};

export {default as EMonitorPage} from "./Page";
export {default as EMonitorHeader} from "./Header";
export {default as EMonitorFooter} from "./Footer";
export {default as EMonitorContainer} from "./Container";
