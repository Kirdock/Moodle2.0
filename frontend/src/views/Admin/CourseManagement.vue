<template>
    <div class="courseManagement">
        <h1>{{$t('course.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link :to="{name:'Admin'}" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('course.management')}}</li>
        </ol>
        <div class="row col-md-12">
            <div class="col-md-4">
                <div class="form-group">
                    <label for="courseSemester_edit" class="control-label">{{ $t('semester.name') }}</label>
                    <select class="form-control" v-model="selectedSemester_edit" id="courseSemester_edit" @change="getCourses(selectedSemester_edit); selectedCourse = {}; selectedCourseId = undefined;">
                        <option v-for="semester in semesters" :value="semester.id" :key="semester.id">
                            {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-md-4">
                <div class="form-group">
                    <label for="selectedCourse" class="control-label">{{ $t('course.name') }}</label>
                    <select class="form-control" v-model="selectedCourseId" id="selectedCourse" @change="getCourse(selectedCourseId)">
                        <option v-for="course in courses" :value="course.id" :key="course.id">
                            {{course.number}} {{course.name}}
                        </option>
                    </select>
                </div>
            </div>
            <div class="col-md-4" style="margin-top: 31px">
                <button class="btn btn-primary" @click="setNewCourse()" style="margin-right: 10px" v-if="selectedSemester_edit !== undefined">
                    <span class="fa fa-sync fa-spin"  v-if="loading.createCourse"></span>
                    <span class="fa fa-plus" v-else></span>
                    {{ $t('new') }}
                </button>
                <b-modal id="modal-new-course" :title="$t('course.title.create')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="createCourse">
                    <label class="control-label requiredField">{{ $t('requiredField') }}</label>
                    <form @submit.prevent="createCourse()" ref="createCourse">
                        <course-info v-model="courseInfo_create" :users="users"></course-info>
                    </form>
                </b-modal>
                <button class="btn btn-primary" v-b-modal="'modal-copy-course'" style="margin-right: 10px" v-if="selectedCourseId" @click="courseCopyId = semestersWithoutSelected[0].id">
                    <span class="fa fa-sync fa-spin"  v-if="loading.copyCourse"></span>
                    <span class="fa fa-copy" v-else></span>
                    {{ $t('copy') }}
                </button>
                <b-modal id="modal-copy-course" :title="$t('course.question.copy')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="copyCourse(selectedCourseId, courseCopyId)">
                    <label for="selectedSemester_copy_course" class="control-label">{{ $t('semester.name') }}</label>
                    <select class="form-control" v-model="courseCopyId" id="selectedSemester_copy_course">
                        <option v-for="semester in semestersWithoutSelected" :value="semester.id" :key="semester.id">
                            {{semester.year}} {{semester.type === 'w' ? $t('semester.winterShortcut') : $t('semester.summerShortcut')}}
                        </option>
                    </select>
                </b-modal>

                <button class="btn btn-danger" v-b-modal="'modal-delete-course'" v-if="selectedCourseId">
                    <span class="fa fa-sync fa-spin" v-if="loading.deleteCourse"></span>
                    <span class="fa fa-trash" v-else></span>
                    {{ $t('delete') }}
                </button>
                <b-modal id="modal-delete-course" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteCourse(selectedCourse.id)">
                    {{$t('course.question.delete')}}
                </b-modal>
            </div>
        </div>
        <b-tabs content-class="mt-3" v-if="selectedCourse.id">
            <b-tab :title="$t('information')" id="courseInfo" active>
                <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
                <div class="form-horizontal col-md-4">
                    <form @submit.prevent="updateCourse()">
                        <course-info v-model="selectedCourse" :users="users"></course-info>
                        
                        <div class="form-inline">
                            <button class="btn btn-primary" type="submit">
                                <span class="fa fa-sync fa-spin" v-if="loading.updateCourse"></span>
                                <span class="fa fa-save" v-else></span>
                                {{ $t('save') }}
                            </button>
                        </div>
                    </form>
                </div>
            </b-tab>
            <b-tab :title="$t('user.assigned')" id="assignedUsers">
                <div class="form-horizontal col-md-7">
                    <div class="form-inline" style="margin-bottom: 10px">
                        <div class="form-group" style="margin-right: 10px">
                            <button class="btn btn-primary" v-b-modal="'modal-kreuzelList'">
                                <span class="fa fa-list"></span>
                                {{$t('kreuzel.name')}}
                            </button>
                        </div>
                        <div class="form-group" style="margin-right: 10px">
                            <button class="btn btn-primary" v-b-modal="'modal-presented'">
                                <span class="fa fa-list"></span>
                                {{$t('presentations')}}
                            </button>
                        </div>
                        <div class="form-group">
                            <button class="btn btn-primary" type="button" @click="getAttendanceList(selectedCourseId)">
                                <span class="fa fa-sync fa-spin" v-if="loading.attendanceList"></span>
                                <span class="fas fa-download" v-else></span>
                                {{$t('attendance.list')}}
                            </button>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="btn btn-primary">
                            <span class="fa fa-sync fa-spin" v-if="loading.fileUpload"></span>
                            <span class="fas fa-upload" v-else></span>
                            {{ $t('uploadCSV') }}
                            <input type="file" class="d-none" id="file" ref="file" accept=".csv" @change="submitUsers()"/>
                        </label>
                    </div>
                    <div class="form-group">
                        <label for="searchUserText" class="control-label">
                            <span class="fas fa-search"></span>
                            {{ $t('search') }}
                        </label>
                        <input id="searchUserText" type="text" class="form-control" v-model="searchUserText">
                    </div>
                    <div class="form-group">
                        <label class="control-label" for="showRoles">
                            <span class="fas fa-filter"></span>
                            {{$t('show')}}
                        </label>
                        <select class="form-control" id="showRoles" v-model="showRoles">
                            <option v-for="role in rolesWithAll" :value="role.key" :key="role.key">
                                {{role.value}}
                            </option>
                        </select>
                    </div>
                    <table class="table table-hover" aria-describedby="assignedUsers">
                        <thead>
                            <th scope="col"></th>
                            <th scope="col">
                                <span class="pointer" @click="setSortOrder(0)">{{$t('matriculationNumber')}}
                                    <span class="fas fa-sort" v-if="sortOrder.index !== 0"></span>
                                    <span class="fas fa-sort-up" v-else-if="sortOrder.type === 0"></span>
                                    <span class="fas fa-sort-up fa-rotate-180" v-else></span>
                                </span>
                            </th>
                            <th scope="col">
                                <span class="pointer" @click="setSortOrder(1)">{{$t('surname')}}
                                    <span class="fas fa-sort" v-if="sortOrder.index !== 1"></span>
                                    <span class="fas fa-sort-up" v-else-if="sortOrder.type === 0"></span>
                                    <span class="fas fa-sort-up fa-rotate-180" v-else></span>
                                </span>
                            </th>
                            <th scope="col">
                                <span class="pointer" @click="setSortOrder(2)">{{$t('forename')}}
                                    <span class="fas fa-sort" v-if="sortOrder.index !== 2"></span>
                                    <span class="fas fa-sort-up" v-else-if="sortOrder.type === 0"></span>
                                    <span class="fas fa-sort-up fa-rotate-180" v-else></span>
                                </span>
                            </th>
                            <th scope="col">
                                <span class="pointer" @click="setSortOrder(3)">{{$t('presentations')}}
                                    <span class="fas fa-sort" v-if="sortOrder.index !== 3"></span>
                                    <span class="fas fa-sort-up" v-else-if="sortOrder.type === 0"></span>
                                    <span class="fas fa-sort-up fa-rotate-180" v-else></span>
                                </span>
                            </th>
                            <th scope="col">{{$t('role')}}</th>
                        </thead>
                        <tbody>
                            <tr v-for="user in filteredUsers" :key="user.matriculationNumber">
                                <td>
                                    <input type="checkbox" class="form-check-input" id="showCheckedUsers" :checked="user.courseRole !== 'n'" @click="user.courseRole = user.courseRole === 'n' ? 's' : 'n'" style="margin-left: 0px">
                                </td>
                                <td>
                                    {{user.matriculationNumber}}
                                </td>
                                <td>
                                    {{user.surname}}
                                </td>
                                <td>
                                    {{user.forename}}
                                </td>
                                <td>
                                    {{user.presentedCount}}
                                </td>
                                <td>
                                    <select class="form-control" v-model="user.courseRole">
                                        <option v-for="role in roles" :value="role.key" :key="role.key">
                                            {{role.value}}
                                        </option>
                                    </select>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="form-inline">
                        <button class="btn btn-primary" @click="updateCourseUsers()">
                            <span class="fa fa-sync fa-spin" v-if="loading.updateUsers"></span>
                            <span class="fa fa-save" v-else></span>
                            {{ $t('save') }}
                        </button>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('exerciseSheets.name')" id="exerciseSheets">
                <div class="form-horizontal">
                    <div class="form-group col-md-6">
                        <button class="btn btn-primary" v-b-modal="'modal-new-exerciseSheet'">
                            <span class="fa fa-sync fa-spin"  v-if="loading.createExerciseSheet"></span>
                            <span class="fa fa-plus" v-else></span>
                            {{$t('new')}}
                        </button>
                        <b-modal id="modal-new-exerciseSheet" :title="$t('exerciseSheet.title.create')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="createExerciseSheet">
                            <form ref="exerciseSheet" @submit.prevent="createExerciseSheet()">
                                <es-info v-model="exerciseSheet_create"></es-info>
                            </form>
                        </b-modal>
                    </div>
                    <div class=" col-md-6">
                        <table class="table table-hover" aria-describedby="exerciseSheets">
                            <thead>
                                <th scope="col">{{$t('name')}}</th>
                                <th scope="col">{{$t('submissionDate')}}</th>
                                <th scope="col">{{$t('actions')}}</th>
                            </thead>
                            <tbody>
                                <tr v-for="(sheet, index) in selectedCourse.exerciseSheets" :key="sheet.id">
                                    <td>
                                        {{sheet.name}}
                                    </td>
                                    <td>
                                        {{new Date(sheet.submissionDate).toLocaleString()}}
                                    </td>
                                    <td>
                                        <router-link :title="$t('edit')" :to="{
                                            name: 'SheetManagement',
                                            params: {
                                                courseId: selectedCourseId,
                                                sheetId: sheet.id
                                            }
                                        }">
                                            <span class="fa fa-edit fa-2x"></span>
                                        </router-link>
                                        <a href="#" :title="$t('download')" @click.prevent="getExerciseSheetPdf(sheet.id)">
                                            <span class="fa fa-eye fa-2x"></span>
                                        </a>
                                        <a href.prevent="#" :title="$t('delete')" v-b-modal="'modal-delete-exerciseSheet'" @click="selectedDeleteExerciseSheet = {sheet, index}">
                                            <span class="fa fa-sync fa-spin fa-2x"  v-if="sheet.deleteLoading"></span>
                                            <span class="fa fa-trash fa-2x" v-else></span>
                                        </a>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </b-tab>
            <b-tab :title="$t('presets')">
                <form @submit.prevent="savePresets()">
                    <div class="form-horizontal">
                        <div class="form-group col-md-6">
                            <label class="control-label" for="defaultDescription">{{$t('descriptionExerciseSheets')}}</label>
                            <div class="document-editor__editable-container">
                                <editor :id="`defaultDescription`" v-model="selectedCourseTemplate"></editor>
                            </div>
                        </div>
                        <div class="form-group">
                            <label :for="`uploadCount`" class="control-label col-md-2 required">{{ $t('exampleUploadLimit') }}</label>
                            <div class="col-md-1">
                                <i-input :id="`uploadCount`" min="0" class="form-control"  v-model="selectedCourse.uploadCount" required></i-input>
                            </div>
                        </div>
                        <div class="col-md-2"> 
                            <button class="btn btn-primary" type="submit">
                                <span class="fa fa-sync fa-spin" v-if="loading.presets"></span>
                                <span class="fa fa-save" v-else></span>
                                {{ $t('save') }}
                            </button>
                        </div>
                    </div>
                </form>
            </b-tab>
        </b-tabs>
        <b-modal id="modal-delete-exerciseSheet" :title="$t('title.delete')" :ok-title="$t('confirm')" :cancel-title="$t('cancel')" @ok="deleteExerciseSheet(selectedDeleteExerciseSheet)">
            {{$t('exerciseSheet.question.delete')}}
        </b-modal>
        <b-modal id="modal-presented" :title="$t('presentations')" size="xl" hide-footer>
            <div class="form-horizontal">
                <div class="form-inline">
                    <div class="form-group">
                        <label for="presentedUser" class="control-label">
                            {{ $t('student') }} <span class="fa fa-sync fa-spin" v-if="loading.presentations"></span>
                        </label>
                        <multiselect v-model="presentedUser" id="presentedUser" open-direction="bottom" @input="getUserKreuzel"
                                    :custom-label="userFormat"
                                    :placeholder="$t('typeToSearch')"
                                    track-by="matriculationNumber"
                                    selectLabel=""
                                    :selectedLabel="$t('selected')"
                                    :options="assignedUsers"
                                    :searchable="true"
                                    :clear-on-select="true"
                                    :close-on-select="true"
                                    :options-limit="300"
                                    :allow-empty="false"
                                    :max-height="600"
                                    deselect-label=""
                                    :show-no-results="false"
                                    >
                                    <span slot="noOptions">{{$t('listEmpty')}}</span>
                        </multiselect>
                    </div>
                    <div class="form-group">
                        <label for="presentedExerciseSheet" class="control-label">{{ $t('exerciseSheet.name') }} ({{$t('optional')}})</label>
                        <multiselect v-model="presentedExerciseSheet" id="presentedExerciseSheet" open-direction="bottom"
                                    :placeholder="$t('typeToSearch')"
                                    @input="presentedExample = {}"
                                    selectLabel=""
                                    :selectedLabel="$t('selected')"
                                    :options="presentedExerciseSheets"
                                    :searchable="true"
                                    :clear-on-select="true"
                                    :close-on-select="true"
                                    :options-limit="300"
                                    :allow-empty="true"
                                    :max-height="600"
                                    :deselect-label="$t('remove')"
                                    :show-no-results="false"
                                    :showNoOptions="true"
                                    >
                                    <span slot="noOptions">{{$t('listEmpty')}}</span>
                        </multiselect>
                    </div>
                    <div class="form-group">
                        <label for="presentedExample" class="control-label">{{ $t('example.name') }}</label>
                        <multiselect v-model="presentedExample" id="presentedExample" open-direction="bottom"
                                    :placeholder="$t('typeToSearch')"
                                    label="exampleName"
                                    track-by="exampleId"
                                    selectLabel=""
                                    :selectedLabel="$t('selected')"
                                    :options="presentedExamples"
                                    :searchable="true"
                                    :clear-on-select="true"
                                    :close-on-select="true"
                                    :options-limit="300"
                                    :allow-empty="false"
                                    :max-height="600"
                                    deselect-label=""
                                    :show-no-results="false"
                                    :showNoOptions="true"
                                    >
                                    <span slot="noOptions">{{$t('listEmpty')}}</span>
                        </multiselect>
                    </div>
                    <div class="form-group" style="margin-top: 22px">
                        <button class="btn btn-primary" @click="addPresentation()">
                            <span class="fa fa-plus"></span>
                        </button>
                    </div>
                </div>
                <div class="form-group">
                        <label class="control-label" for="showExerciseSheets">
                            <span class="fas fa-filter"></span>
                            {{$t('exerciseSheets.name')}}
                        </label>
                        <select class="form-control" id="showExerciseSheets" v-model="showExerciseSheet">
                            <option v-for="sheet in exerciseSheetSelect" :value="sheet" :key="sheet">
                                {{sheet}}
                            </option>
                        </select>
                    </div>
                <table class="table table-hover" aria-describedby="modal-presented">
                    <thead>
                        <th scope="col">{{$t('matriculationNumber')}}</th>
                        <th scope="col">{{$t('surname')}}</th>
                        <th scope="col">{{$t('forename')}}</th>
                        <th scope="col">{{$t('exerciseSheet.name')}}</th>
                        <th scope="col">{{$t('example.name')}}</th>
                        <th scope="col">{{$t('actions')}}</th>
                    </thead>
                    <tbody>
                        <tr v-for="(presented, index) in filteredExerciseSheets" :key="presented.id">
                            <td>
                                {{presented.matriculationNumber}}
                            </td>
                            <td>
                                {{presented.surname}}
                            </td>
                            <td>
                                {{presented.forename}}
                            </td>
                            <td>
                                {{presented.exerciseSheetName}}
                            </td>
                            <td>
                                {{presented.exampleName}}
                            </td>
                            <td>
                                <a href="#" :title="$t('delete')" @click.prevent="updatePresented({matriculationNumber: presented.matriculationNumber}, {exampleId: presented.exampleId}, false, index)">
                                    <span class="fa fa-sync fa-spin fa-2x" v-if="presented.deleteLoading"></span>
                                    <span class="fa fa-trash fa-2x" v-else></span>
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </b-modal>
        <b-modal id="modal-kreuzelList" :title="$t('kreuzel.name')" size="xl" hide-footer>
            <kreuzel-list :exerciseSheets="selectedCourse.exerciseSheets"></kreuzel-list>
        </b-modal>
    </div>
</template>

<script>
import CourseInfo from '@/components/CourseInfo.vue';
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
import KreuzelList from '@/components/KreuzelList.vue';
import {userManagement, dateManagement, fileManagement} from '@/plugins/global';
import Editor from '@/components/Editor.vue';
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.min.css';

export default {
    components: {
        'course-info': CourseInfo,
        'es-info': ExerciseSheetInfo,
        'kreuzel-list': KreuzelList,
        Editor,
        Multiselect
    },
    data(){
        return {
            courseInfo_create: {},
            selectedSemester_edit: undefined,
            selectedCourse: {},
            selectedCourseId: undefined,
            courseCopyId: undefined,
            courses: [],
            searchUserText: undefined,
            showRoles: 'z',
            courseUsers: [],
            semesters: [],
            exerciseSheet_create: {},
            users: [],
            loading: {
                fileUpload: false,
                presets: false,
                createCourse: false,
                updateCourse: false,
                updateUsers: false,
                deleteCourse: false,
                attendanceList: false,
                presentations: false
            },
            selectedCourseTemplate: undefined,
            selectedDeleteExerciseSheet: undefined,
            presentedUser: undefined,
            presentedExample: {},
            presentedExerciseSheet: undefined,
            presentedExerciseSheets: [],
            showExerciseSheet: undefined,
            userKreuzel: [],
            sortOrder:{
                index: 0,
                type: 0 //0 => DESC, 1 => ASC
            }
        }
    },
    async created(){
        this.showExerciseSheet = this.$t('all');
        this.resetExerciseSheet();
        if(this.$store.getters.userInfo.isAdmin){
            await this.getUsers();
        }
        if(this.$route.query.courseId){
            this.selectedCourseId = this.$route.query.courseId;
            this.getCourse(this.selectedCourseId, true);
        }
        else{
            this.getSemesters();
        }
        
    },
    computed: {
        roles(){
            return userManagement.roles();
        },
        rolesWithAll(){
            return userManagement.rolesAllAssign();
        },
        assignedUsers(){
            return userManagement.filteredUsers({users: this.courseUsers, role: 's'});
        },
        filteredUsers(){
            const users = userManagement.filteredUsers({users: this.courseUsers, role: this.showRoles, searchText: this.searchUserText});
            if(this.sortOrder.index !== 0 || this.sortOrder.type !== 0){
                const key = this.sortOrder.index === 0 ? 'matriculationNumber' : this.sortOrder.index === 1 ? 'surname' : this.sortOrder.index === 2 ? 'forename' : 'presentedCount';
                if(this.sortOrder.type === 0){
                    users.sort((a,b) => a[key].toString().localeCompare(b[key].toString()) || a['matriculationNumber'].localeCompare(b['matriculationNumber']));
                }
                else{ 
                    users.sort((a,b) => b[key].toString().localeCompare(a[key].toString()) || a['matriculationNumber'].localeCompare(b['matriculationNumber']));
                }
            }
            return users;
        },
        semestersWithoutSelected(){
            return this.semesters.filter(semester => semester.id !== this.selectedSemester_edit);
        },
        presentedExamples(){
            let examples = [];
            if(this.presentedExerciseSheet){
                examples = this.userKreuzel.find(sheet => sheet.exerciseSheetName === this.presentedExerciseSheet).examples;
            }
            else{
                for(let sheet of this.userKreuzel){
                    examples = examples.concat(sheet.examples);
                }
            }
            return examples;
        },
        exerciseSheetSelect(){
            return this.selectedCourse.id ? [this.$t('all')].concat(this.selectedCourse.exerciseSheets.map(sheet => sheet.name)) : [];
        },
        filteredExerciseSheets(){
            let presented;
            if(this.showExerciseSheet === this.$t('all')){
                presented = this.selectedCourse.presented;
            }
            else{
                presented = this.selectedCourse.presented.filter(presentation => presentation.exerciseSheetName === this.showExerciseSheet);
            }
            return presented;
        }
    },
    methods:{
        setNewCourse(){
            for(const key in this.courseInfo_create){ //this.courseInfo_create = {} kills reference
                this.courseInfo_create[key] = undefined;
            }
            this.$bvModal.show('modal-new-course');
        },
        setSortOrder(index){
            if(this.sortOrder.index !== index){
                this.sortOrder = {
                    index: index,
                    type: 0
                }
            }
            else if(this.sortOrder.type === 0){
                this.sortOrder.type = 1;
            }
            else{
                this.sortOrder.type = 0;
            }
        },
        addPresentation(){
            if(this.presentedUser && this.presentedUser.matriculationNumber && this.presentedExample && this.presentedExample.exampleId){
                if(this.selectedCourse.presented.some(presentedExample => presentedExample.matriculationNumber === this.presentedUser.matriculationNumber && presentedExample.exampleId === this.presentedExample.exampleId)){
                    this.$bvToast.toast(this.$t('presentation.exists'), {
                        title: this.$t('warning'),
                        variant: 'warning',
                        appendToast: true
                    });
                }
                else{
                    this.updatePresented(this.presentedUser, this.presentedExample, true);
                }
            }
        },
        async updatePresented(user, example, hasPresented, index){
            try{
                if(!hasPresented){
                    this.$set(this.selectedCourse.presented[index], 'deleteLoading', true);
                }
                await this.$store.dispatch('updatePresented', {matriculationNumber: user.matriculationNumber, exampleId: example.exampleId, hasPresented});
                if(index !== undefined){
                    this.selectedCourse.presented.splice(index,1);
                }
                else{
                    try{
                        const response = await this.$store.dispatch('getCoursePresented',this.selectedCourseId);
                        this.selectedCourse.presented = response.data;
                    }
                    catch{
                        this.$bvToast.toast(this.$t('presentation.error.get'), {
                            title: this.$t('error'),
                            variant: 'danger',
                            appendToast: true
                        });
                    }
                }
                this.getCourseUsers(this.selectedCourseId);
                this.$bvToast.toast(hasPresented ? this.$t('presentation.saved') : this.$t('presentation.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                let message = this.$t('presentation.error.save');
                if(index !== undefined){
                    this.$set(this.selectedCourse.presented[index], 'deleteLoading', undefined);
                    message = this.$t('presentation.error.delete');
                }
                this.$bvToast.toast(message, {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }

        },
        setCourseQuery(courseId){
            if(this.$route.query.courseId !== courseId){
                this.$router.push({ query: { courseId }});
            }
        },
        userFormat(user){
            return `${user.matriculationNumber} ${user.surname} ${user.forename}`;
        },
        async getUserKreuzel(){
            this.loading.presentations = true;
            try{
                this.presentedExample = {};
                const response = await this.$store.dispatch('getUserKreuzel', {matriculationNumber: this.presentedUser.matriculationNumber, courseId: this.selectedCourseId});
                this.presentedExerciseSheet = undefined;
                this.userKreuzel = response.data;
                this.presentedExerciseSheets = response.data.map(sheet => sheet.exerciseSheetName);
            }
            catch{
                this.$bvToast.toast(this.$t('kreuzel.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.presentations = false;
            }
        },
        async getAttendanceList(courseId){
            this.loading.attendanceList = true;
            try{
                const response = await this.$store.dispatch('getAttendanceList', courseId);
                fileManagement.download(response);
            }
            catch{
                this.$bvToast.toast(this.$t('attendance.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.attendanceList = false;
            }
        },
        async getUsers(){
            try{
                const response = await this.$store.dispatch('getUsers');
                this.users = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('users.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async submitUsers(){
            this.loading.fileUpload = true;
            const formData = new FormData();
            formData.append('file',this.$refs.file.files[0]);
            formData.append('id', this.selectedCourseId)
            this.$refs.file.value = '';
            try{
                await this.$store.dispatch('assignCourseUsers', formData);
                this.getCourseUsers(this.selectedCourseId);
                this.$bvToast.toast(this.$t('course.usersSaved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('course.error.usersSave'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.fileUpload = false;
            }
        },
        resetExerciseSheet(){
            this.exerciseSheet_create = {
                submissionDate: dateManagement.midnightDateTime(),
                issueDate: dateManagement.currentDateTime(),
                includeThird: false
            }
        },
        async createExerciseSheet(modal){
            if(modal && !this.$refs.exerciseSheet.checkValidity()){
                modal.preventDefault();
                this.$refs.exerciseSheet.reportValidity();
            }
            else{
                this.loading.createExerciseSheet = true;
                this.exerciseSheet_create.courseId = this.selectedCourseId;
                this.exerciseSheet_create.description = this.selectedCourse.descriptionTemplate;
                try{
                    await this.$store.dispatch('createExerciseSheet', this.exerciseSheet_create);
                    this.$bvModal.hide('modal-new-exerciseSheet');
                    if(this.exerciseSheet_create.courseId === this.selectedCourseId){
                        this.getExerciseSheets(this.selectedCourseId);
                    }
                    this.resetExerciseSheet();
                    this.$bvToast.toast(this.$t('exerciseSheet.created'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                    
                }
                catch{
                    this.$bvToast.toast(this.$t('exerciseSheet.error.create'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loading.createExerciseSheet = false;
                }
            }
        },
        async deleteExerciseSheet(selectedSheetData){
            this.$set(selectedSheetData.sheet,'deleteLoading', true);
            const courseId = this.selectedCourseId;
            try{
                await this.$store.dispatch('deleteExerciseSheet', selectedSheetData.sheet.id);
                if(courseId === this.selectedCourseId){
                    this.selectedCourse.exerciseSheets.splice(selectedSheetData.index,1);
                }
                
                this.$bvToast.toast(this.$t('exerciseSheet.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
                
            }
            catch{
                this.$set(selectedSheetData.sheet,'deleteLoading', false);
                this.$bvToast.toast(this.$t('exerciseSheet.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async getExerciseSheets(id){
            try{
                const response = await this.$store.dispatch('getExerciseSheets', {id});
                if(this.selectedCourseId === id){
                    this.selectedCourse.exerciseSheets = response.data;
                }
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async updateCourseUsers(){
            this.loading.updateUsers = true;
            const id = this.selectedCourse.id;
            const data = this.courseUsers.filter(user => user.courseRole !== user.oldRole).map(user =>{
                return {
                    courseId: id,
                    matriculationNumber: user.matriculationNumber,
                    role: user.courseRole
                };
            });
            try{
                await this.$store.dispatch('updateCourseUsers', data);
                this.$bvToast.toast(this.$t('course.usersSaved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('course.error.usersSave'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.updateUsers = false;
            }
        },
        async createCourse(modal){
            if(modal && !this.$refs.createCourse.checkValidity()){
                modal.preventDefault();
                this.$refs.createCourse.reportValidity();
            }
            else{
                this.courseInfo_create.semesterId = this.selectedSemester_edit;
                this.loading.createCourse = true;
                const {semesterId} = this.courseInfo_create;
                const {...temp} = this.courseInfo_create; //create a copy in order to have consistent data after api call (during creation the form could be called again)
                try{
                    const response = await this.$store.dispatch('createCourse',temp);
                    this.$bvToast.toast(this.$t('course.created'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                    this.courseInfo_create = {semesterId};

                    if(semesterId === this.selectedSemester_edit){
                        this.courses.push({
                            id: response.data.id,
                            name: temp.name,
                            number: temp.number
                        })
                        this.courses.sort((a,b) => a.number.localeCompare(b.number));
                        this.selectedCourseId = response.data.id;
                        this.getCourse(response.data.id);
                    }
                }
                catch{
                    this.$bvToast.toast(this.$t('course.error.create'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                finally{
                    this.loading.createCourse = false;
                }
            }
        },
        async updateCourse(){
            this.loading.updateCourse = true;
            const {exerciseSheets, ...data} = this.selectedCourse;
            try{
                await this.$store.dispatch('updateCourse', data);
                for(const course of this.courses){
                    if(course.id === data.id){
                        course.name = data.name;
                        course.number = data.number;
                    }
                }
                this.$bvToast.toast(this.$t('course.saved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch(error){
                if(error.response.data.errorResponseCode === 474){
                    this.$bvToast.toast(this.$t('course.error.duplicate'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                else{
                    this.$bvToast.toast(this.$t('course.error.save'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
            }
            finally{
                this.loading.updateCourse = false;
            }
        },
        async savePresets(){
            this.loading.presets = true;
            try{
                await this.$store.dispatch('updateCoursePresets', {id: this.selectedCourse.id, description: this.selectedCourseTemplate, uploadCount: this.selectedCourse.uploadCount});
                this.selectedCourse.descriptionTemplate = this.selectedCourseTemplate;
                this.$bvToast.toast(this.$t('course.presetsUpdated'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('course.error.presetsUpdate'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.presets = false;
            }
        },
        async copyCourse(courseId, semesterId){
            this.loading.copyCourse = true;
            try{
                const response = await this.$store.dispatch('copyCourse', {courseId, semesterId});
                this.selectedSemester_edit = semesterId;
                this.getCourses(semesterId);
                this.selectedCourseId = response.data.id;
                this.getCourse(response.data.id);
                
                this.$bvToast.toast(this.$t('course.copied'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch(error){
                if(error.response.data.errorResponseCode === 475){
                    this.$bvToast.toast(this.$t('course.error.duplicate'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
                else{
                    this.$bvToast.toast(this.$t('course.error.copy'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }
            }
            finally{
                this.loading.copyCourse = false;
            }
        },
        async getCourse(courseId, revertOnError){
            this.getCourseUsers(courseId);
            try{
                const response = await this.$store.dispatch('getCourse',{courseId});
                this.selectedCourse = response.data;
                if(this.semesters.length === 0){
                    this.getSemesters(this.selectedCourse.semesterId);
                }
                this.selectedCourseTemplate = this.selectedCourse.descriptionTemplate; //no reference; update on save
            }
            catch{
                this.$bvToast.toast(this.$t('course.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });

                if(revertOnError && this.semesters.length === 0){
                    this.getSemesters();
                    this.setCourseQuery();
                }
            }
        },
        async getCourses(id){
            try{
                const response = await this.$store.dispatch('getCourses',{id});
                this.courses = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('courses.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async deleteCourse(id){
            this.loading.deleteCourse = true;
            try{
                await this.$store.dispatch('deleteCourse',{id});
                if(id == this.selectedCourseId){ //check if user did not select another course during delete
                    this.selectedCourse = {};
                    this.setCourseQuery();
                }
                let index = -1;
                this.courses.forEach((course, $index) => {
                    if(course.id === id){
                        index = $index;
                    }
                });
                if(index !== -1){
                    this.courses.splice(index, 1);
                }

                this.$bvToast.toast(this.$t('course.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('course.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.deleteCourse = false;
            }
        },
        async getCourseUsers(courseId){
            try{
                const response = await this.$store.dispatch('getUsers',{courseId});
                this.courseUsers = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('users.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async getSemesters(selectSemesterId){
            try{
                const response = await this.$store.dispatch('getSemesters');
                this.semesters = response.data;
                if(this.semesters.length !== 0){
                    this.courseInfo_create.semesterId = this.courseCopyId = this.semesters[0].id;
                    this.selectedSemester_edit = selectSemesterId || this.semesters[0].id;
                    this.getCourses(this.selectedSemester_edit);
                }
            }
            catch{
                this.$bvToast.toast(this.$t('semester.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async getExerciseSheetPdf(id){
            try{
                const response = await this.$store.dispatch('getExerciseSheetPdf', id);
                fileManagement.download(response);
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: 'Fehler',
                    variant: 'danger',
                    appendToast: true
                });
            }
        }
    },
    watch:{
        selectedCourseId: function (newValue, oldValue) {
            this.setCourseQuery(newValue);
        },
        '$route.query.courseId': function(newValue, oldValue){
            if(newValue !== this.selectedCourseId){
                this.selectedCourseId = newValue;
                if(newValue !== undefined){
                    this.getCourse(this.selectedCourseId)
                }
            }
        }
    }
}
</script>