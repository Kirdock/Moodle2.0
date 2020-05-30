<template>
    <div class="SheetManagement">
        <h1>{{$t('course.management')}}</h1>
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <router-link to="/">{{ $t('home') }}</router-link>
            </li>
            <li class="breadcrumb-item">
                <router-link to="/Admin" >{{ $t('admin') }}</router-link>
            </li>
            <li class="breadcrumb-item active">
                <router-link to="/Admin/CourseManagement" >{{$t('course.management')}}</router-link></li>
            <li class="breadcrumb-item active">{{sheetInfo.courseNumber}} {{sheetInfo.courseName}}</li>
            <li class="breadcrumb-item active">{{$t('exerciseSheet.name')}}</li>
            <li class="breadcrumb-item active">{{sheetInfo.name}}</li>
        </ol>
        <label class="control-label requiredField" style="margin-left: 10px">{{ $t('requiredField') }}</label>
            <b-tabs class="mt-3" v-model="activeTab" id="exerciseSheetTab">
                <b-tab :title="$t('information')" :class="'fixed'">
                    
                    <div class="form-horizontal col-md-5 fixed">
                        <form ref="exerciseSheet" @submit.prevent="updateInfo()">
                            <es-info v-model="sheetInfo"></es-info>
                            <div class="form-inline">
                                <button class="btn btn-primary" type="submit">
                                    <span class="fa fa-sync fa-spin" v-if="loading_updateInformation"></span>
                                    <span class="fa fa-save" v-else></span>
                                    {{ $t('save') }}
                                </button>
                            </div>
                        </form>
                    </div>
                </b-tab>
                
                <b-tab class="item" v-for="(example, index) in sheetInfo.examples" :key="example.id" :title="example.name" @click="setSelectedExample()">
                    <div style="margin-top: 10px; font-size: 30px" v-if="isSubExample">
                        <a href="#" @click.prevent="setSelectedExample()">
                            {{example.name}}
                        </a>
                        <span class="fas fa-chevron-right"></span>
                        <span>{{selectedExample.name}}</span>
                    </div>
                    <form @submit.prevent="updateExample(selectedExample || example)" :ref="'formExample'+index.toString()">
                        <example-info :selectedDeleteExample="selectedDeleteExample" :isSubExample="isSubExample" :buildExample="buildExample" :setSelectedExample="setSelectedExample" :setDeleteExample="setDeleteExample" :value="selectedExample || example" :deleteExample="deleteExample"></example-info>
                        <div class="form-inline" style="margin-left: 10px; margin-top: 10px" v-if="!isSubExample">
                            <button class="btn btn-primary" type="submit">
                                <span class="fa fa-sync fa-spin"  v-if="example.loading"></span>
                                <span class="fa fa-save" v-else></span>
                                {{ $t('save') }}
                            </button>
                            <button class="btn btn-danger" style="margin-left: 10px" v-b-modal="'modal-delete-example'" type="button" @click="setDeleteExample(example.id, index, sheetInfo.examples)">
                                <span class="fa fa-trash"></span>
                                {{$t('delete')}}
                            </button>
                        </div>
                    </form>
                </b-tab>
                
                <template v-slot:tabs-end class="fixed">
                    <b-nav-item role="presentation" @click.prevent="newExample()" href="#"><span class="fa fa-plus"></span> {{$t('example.new')}}</b-nav-item>
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
            onEnd: evt =>{
                if(evt.oldIndex !== evt.newIndex){
                    const changedOrders = orderManagement.moveTo(this.sheetInfo.examples, evt.oldIndex-1, evt.newIndex-1); //-1 because "Information"-Tab is before
                    this.$store.dispatch('updateExampleOrder', changedOrders).then(()=>{
                        orderManagement.sort(this.sheetInfo.examples);
                    }).catch(()=>{
                        orderManagement.revertSort(this.sheetInfo.examples);
                        const array = sortable.toArray();
                        orderManagement.move(array, evt.newIndex, evt.oldIndex);
                        sortable.sort(array);
                        
                        this.$bvToast.toast(this.$t('sortError'), {
                            title: this.$t('error'),
                            variant: 'danger',
                            appendToast: true
                        });
                    })
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
            loading_updateInformation: false,
            activeTab: 0,
            selectedDeleteExample: {}
        }
    },
    methods: {
        getSheet(sheedId){
            this.$store.dispatch('getExerciseSheet', sheedId).then(response =>{
                this.sheetInfo = response.data;
            }).catch(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.error.get'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            })
        },
        updateInfo(){
            this.loading_updateInformation = true;
            const {examples, ...data} = this.sheetInfo;
            this.$store.dispatch('updateExerciseSheet', data).then(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.saved'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }).catch(()=>{
                this.$bvToast.toast(this.$t('exerciseSheet.error.save'), {
                    title: this.$t('error'),
                    variant: 'danger',
                    appendToast: true
                });
            }).finally(()=>{
                this.loading_updateInformation = false;
            });
        },
        setDeleteExample(id, index, array){
            this.selectedDeleteExample.id = id;
            this.selectedDeleteExample.index = index;
            this.selectedDeleteExample.array = array;
        },
        updateExample(example){
            example.loading = true;
            const {loading, subExamples, ...data} = example;
            if(example.id){
                this.$store.dispatch('updateExample', data).then(response=>{
                    this.$bvToast.toast(this.$t('example.saved'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('example.error.save'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }).finally(()=>{
                    example.loading = false;
                });
            }
            else{
                this.$store.dispatch('createExample', data).then(response=>{
                    example.id = response.data.id;
                    this.$bvToast.toast(this.$t('example.created'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('example.error.create'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                }).finally(()=>{
                    example.loading = false;
                });
            }
        },
        newExample(){
            this.sheetInfo.examples.push(this.buildExample(this.$t('example.name'), this.sheetInfo.examples.length, true));
            this.$nextTick(()=>{
                this.$nextTick(() => {
                    requestAnimationFrame(() => {
                        this.activeTab = this.sheetInfo.examples.length;
                    })
                })
            });
        },
        buildExample(name, order, isParent){
            return {
                name: `${name} ${order+1}`,
                parentId: isParent ? undefined : this.sheetInfo.examples[this.activeTab -1].id,
                exerciseSheetId: this.sheetId,
                weighting: 1,
                points: 0,
                subExamples: [],
                order,
                supportedFileTypes: [],
                customFileTypes: [],
                mandatory: false,
                validator: ''
            }
        },
        deleteExample(id, index){
            if(id){
                //server needs to adjust/update all orders
                this.$store.dispatch('deleteExample', id).then(()=>{
                    orderManagement.deletedAt(this.selectedDeleteExample.array, index);
                    this.$bvToast.toast(this.$t('example.deleted'), {
                        title: this.$t('success'),
                        variant: 'success',
                        appendToast: true
                    });
                }).catch(()=>{
                    this.$bvToast.toast(this.$t('example.error.delete'), {
                        title: this.$t('error'),
                        variant: 'danger',
                        appendToast: true
                    });
                })
            }
            else{
                orderManagement.deletedAt(this.selectedDeleteExample.array, index);
                this.$bvToast.toast(this.$t('example.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
        },
        setSelectedExample(example){
            this.selectedExample = example;
        }
    }
}
</script>