declare module "fileChooser" {
    export class fileChooser {
        static open (accept: string, successCallback: ()=>any, failureCallback: ()=>any): Promise<FileChooserResult>;
        static open (successCallback: ()=>any, failureCallback: ()=>any): Promise<FileChooserResult>;
    }
}

interface FileChooserResult {
    mediaType: string;
    name: string;
    uri: string;
    content: string;
}
