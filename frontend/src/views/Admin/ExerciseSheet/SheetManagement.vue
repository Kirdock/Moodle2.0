<template>
    <div class="SheetManagement">
        <h1>{{$t('course.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link :to="{name:'Admin'}" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link :to="{name:'CourseManagement'}">{{$t('course.management')}}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link :to="{
                                name: 'CourseManagement',
                                query: {
                                    courseId: sheetInfo.courseId,
                                }
                            }">
                    {{sheetInfo.courseNumber}} {{sheetInfo.courseName}}
                </router-link>
            </li>
            <li class="breadcrumb-item active">{{$t('exerciseSheet.name')}}</li>
            <li class="breadcrumb-item active">{{sheetInfo.name}}</li>
        </ol>
        <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
            <b-tabs class="mt-3" v-model="activeTab" id="exerciseSheetTab">
                <b-tab :title="$t('information')" :class="'fixed'">
                    
                    <div class="form-horizontal col-md-5 fixed" v-if="sheetInfo.includeThird !== undefined">
                        <form ref="exerciseSheet" @submit.prevent="updateInfo()">
                            <es-info v-model="sheetInfo"></es-info>
                            <div class="form-inline" style="margin-top: 10px">
                                <button class="btn btn-primary" type="submit">
                                    <span class="fa fa-sync fa-spin" v-if="loading.updateInformation"></span>
                                    <span class="fa fa-save" v-else></span>
                                    {{ $t('save') }}
                                </button>
                            </div>
                        </form>
                    </div>
                </b-tab>
                
                <b-tab class="item" v-for="(example, index) in sheetInfo.examples" :key="example.id" :title="example.name" @click="setSelectedExample(undefined, {example, index})">
                    <div style="margin-top: 10px; font-size: 30px" v-if="isSubExample">
                        <a href="#" @click.prevent="setSelectedExample(undefined, {example, index})">
                            {{example.name}}
                        </a>
                        <span class="fas fa-chevron-right"></span>
                        <span>{{selectedExample.name}}</span>
                    </div>
                    <form @submit.prevent="updateExample(selectedExample || example)" :ref="`formExample${index}`">
                        <example-info :ref="`eInfo${index}`" :uploadCount="sheetInfo.uploadCount" :selectedDeleteExample="selectedDeleteExample" :isSubExample="isSubExample" :buildExample="buildExample" :setSelectedExample="setSelectedExample" :setDeleteExample="setDeleteExample" :value="selectedExample || example"></example-info>
                        <div class="form-inline" style="margin-left: 10px; margin-top: 10px" v-if="!isSubExample">
                            <button class="btn btn-primary" type="submit">
                                <span class="fa fa-sync fa-spin"  v-if="example.loading"></span>
                                <span class="fa fa-save" v-else></span>
                                {{ $t('save') }}
                            </button>
                            <button class="btn btn-danger" style="margin-left: 10px" v-b-modal="'modal-delete-example'" type="button" @click="setDeleteExample(example.id, index, sheetInfo.examples)">
                                <span class="fa fa-sync fa-spin"  v-if="example.deleteLoading"></span>
                                <span class="fa fa-trash" v-else></span>
                                {{$t('delete')}}
                            </button>
                        </div>
                    </form>
                </b-tab>
                
                <template v-slot:tabs-end class="fixed">
                    <b-nav-item role="presentation" @click.prevent="createExample()" href="#">
                        <span class="fa fa-sync fa-spin" v-if="loading.createExample"></span>
                        <span class="fa fa-plus" v-else></span>
                        {{$t('example.new')}}
                    </b-nav-item>
                </template>
            </b-tabs>
        
        
        <b-modal id="modal-delete-example" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteExample(selectedDeleteExample.id, selectedDeleteExample.index)">
            {{$t('example.question.delete')}}
        </b-modal>
    </div>
</template>



<script>
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
import ExampleInfo from '@/components/ExampleInfo.vue';
import {orderManagement} from '@/plugins/global';
import Sortable from "sortablejs";

export default {
    props: ['courseId', 'sheetId', 'name'],
    components: {
        'es-info': ExerciseSheetInfo,
        'example-info': ExampleInfo
    },
    mounted(){
        const el = document.querySelector('.nav-tabs');
        const sortable = Sortable.create(el, {
            animation: 200,
            // filter: '.fixed', //does not work because b-tabs discards defined classes
            onMove: evt => {
                const lastIndex = (this.sheetInfo.examples.length + 1);
                const oldIndex = Sortable.utils.index(evt.dragged, '>*');
                const newIndex = Sortable.utils.index(evt.related, '>*')
                return oldIndex !== 0 && oldIndex !== lastIndex && newIndex !== 0 && newIndex !== lastIndex; //exclude "Information" and "new example" tab
            },
            onEnd: async evt =>{
                if(evt.oldIndex !== evt.newIndex){
                    const changedOrders = orderManagement.moveTo(this.sheetInfo.examples, evt.oldIndex-1, evt.newIndex-1); //-1 because "Information"-Tab is before
                    try{
                        await this.$store.dispatch('updateExampleOrder', changedOrders);
                        orderManagement.sort(this.sheetInfo.examples);
                    }
                    catch{
                        orderManagement.revertSort(this.sheetInfo.examples);
                        const array = sortable.toArray();
                        orderManagement.move(array, evt.newIndex, evt.oldIndex);
                        sortable.sort(array);
                        
                        this.$bvToast.toast(this.$t('sortError'), {
                            title: this.$t('error'),
                            variant: 'danger',
                            appendToast: true
                        });
                    }
                }
            }
        });
    },
    created(){
        this.getSheet(this.sheetId);
    },
    computed:{
        isSubExample(){
            return !!this.selectedExample;
        }
    },
    data(){
        return {
            sheetInfo: {},
            selectedExample: undefined,
            loading: {
                createExample: false,
                deleteExample: false,
                updateInformation: false
            },
            activeTab: 0,
            selectedDeleteExample: {}
        }
    },
    methods: {
        setActiveTab(index){
            this.$nextTick(()=>{
                this.$nextTick(() => {
                    requestAnimationFrame(() => {
                        this.activeTab = index;
                    })
                })
            });
        },
        async getSheet(sheedId){
            try{
                const response = await this.$store.dispatch('getExerciseSheet', sheedId);
                this.sheetInfo = response.data;
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        async updateInfo(){
            this.loading.updateInformation = true;
            const {examples, ...data} = this.sheetInfo;
            try{
                await this.$store.dispatch('updateExerciseSheet', data);
                this.$bvToast.toast(this.$t('exerciseSheet.saved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('exerciseSheet.error.save'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }finally{
                this.loading.updateInformation = false;
            }
        },
        setDeleteExample(id, index, array){
            this.selectedDeleteExample.id = id;
            this.selectedDeleteExample.index = index;
            this.selectedDeleteExample.array = array;
        },
        async updateExample(example){
            example.loading = true;
            const {loading, subExamples, ...data} = example;
            try{
                await this.$store.dispatch('updateExample', data);
                this.$bvToast.toast(this.$t('example.saved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$bvToast.toast(this.$t('example.error.save'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                example.loading = false;
            }
        },
        async createExample(){
            this.loading.createExample = true;
            const example = this.buildExample(this.$t('example.name'), this.sheetInfo.examples.length, true);
            try{
                const response = await this.$store.dispatch('createExample', example);
                example.id = response.data.id;
                this.sheetInfo.examples.push(example);
                this.setActiveTab(this.sheetInfo.examples.length);
            }
            catch{
                this.$bvToast.toast(this.$t('example.error.create'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
            finally{
                this.loading.createExample = false;
            }
        },
        buildExample(name, order, isParent){
            return {
                name: `${name} ${order+1}`,
                description: '',
                parentId: isParent ? undefined : this.sheetInfo.examples[this.activeTab -1].id,
                exerciseSheetId: this.sheetId,
                weighting: 1,
                points: 0,
                subExamples: [],
                order,
                supportedFileTypes: [],
                customFileTypes: []
            }
        },
        async deleteExample(id, index){
            try{
                this.$set(this.selectedDeleteExample.array[index], 'deleteLoading', true);
                await this.$store.dispatch('deleteExample', id);
                orderManagement.deletedAt(this.selectedDeleteExample.array, index);
                this.$bvToast.toast(this.$t('example.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
            catch{
                this.$set(this.selectedDeleteExample.array[index], 'deleteLoading',undefined);
                this.$bvToast.toast(this.$t('example.error.delete'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }
        },
        setSelectedExample(example, parentInfo){
            this.selectedExample = example;
            if(parentInfo){
                let child = this.$refs[`eInfo${parentInfo.index}`];
                if(Array.isArray(child)){
                    child = child[0];
                }
                child.forceUpdate(parentInfo.example.description);
            }
        }
    }
}
</script>