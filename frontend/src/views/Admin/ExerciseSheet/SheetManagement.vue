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
        <b-tabs class="mt-3">
            <b-tab :title="$t('information')" :active="activeTab === 0">
                <div class="form-horizontal col-md-4">
                    <form ref="exerciseSheet" @submit.prevent="updateInfo()">
                        <es-info v-model="sheetInfo"></es-info>
                        <div class="form-inline">
                            <button class="btn btn-primary" type="submit">
                                <span class="fa fa-save"></span>
                                {{ $t('save') }}
                            </button>
                            <div class="offset-md-1 form-inline" v-if="loading_updateInformation">
                                <span class="fa fa-sync fa-spin"></span>
                                <label class="control-label">{{ $t('loading') }}...</label>
                            </div>
                        </div>
                    </form>
                </div>
            </b-tab>
            <b-tab v-for="(example, index) in sheetInfo.examples" :key="example.id" :title="example.name" :active="activeTab === (index+1)" @click="setSelectedExample()">
                <div style="margin-top: 10px; font-size: 30px" v-if="selectedExample">
                    <a href="javascript:void(0)" @click="setSelectedExample()">
                        {{example.name}}
                    </a>
                    <span class="fas fa-chevron-right"></span>
                    <span>{{selectedExample.name}}</span>
                </div>
                <form @submit.prevent="updateExample(example)" :ref="'formExample'+index.toString()">
                    <example-info :adjustOrder="adjustSubExampleOrder" :buildExample="buildExample" :hasSubExamples="!selectedExample" :setSelectedExample="setSelectedExample" :value="selectedExample ? selectedExample : sheetInfo.examples[index]"></example-info>
                    <div class="form-inline" style="margin-left: 10px; margin-top: 10px">
                        <button class="btn btn-primary" type="submit">
                            <span class="fa fa-sync fa-spin"  v-if="example.loading"></span>
                            <span class="fa fa-save" v-else></span>
                            {{ $t('save') }}
                        </button>
                        <button class="btn btn-danger" style="margin-left: 10px" v-b-modal="'modal-delete-example'">
                            <span class="fa fa-trash"></span>
                            {{$t('delete')}}
                        </button>
                        <b-modal id="modal-delete-example" :title="$t('title.delete')" :ok-title="$t('yes')" :cancel-title="$t('no')" @ok="deleteExample(example.id, index)">
                            {{$t('example.question.delete')}}
                        </b-modal>
                    </div>
                </form>
            </b-tab>
            <template v-slot:tabs-end>
                <li role="presentation" class="nav-item">
                    <a href="javascript:void(0)" class="nav-link" @click="newExample()">
                        <span class="fa fa-plus"></span>
                        {{$t('example.new')}}
                    </a>
                </li>
            </template>
        </b-tabs>
    </div>
</template>



<script>
import ExerciseSheetInfo from '@/components/ExerciseSheetInfo.vue';
import ExampleInfo from '@/components/ExampleInfo.vue';
export default {
    props: ['courseId', 'sheetId', 'name'],
    components: {
        'es-info': ExerciseSheetInfo,
        'example-info': ExampleInfo
    },
    created(){
        this.getSheet(this.sheetId);
    },
    data(){
        return {
            sheetInfo: {},
            selectedExample: undefined,
            loading_updateInformation: false,
            activeTab: 0
        }
    },
    methods: {
        getSheet(sheedId){
            this.$store.dispatch('getExerciseSheet', sheedId).then(response =>{
                response.data.examples = []; //remove later
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
        updateExample(example){
            if(this.areSubExamplesValid(example)){
                example.loading = true;
                const {loading, ...data} = example;
                if(example.id){
                    this.$store.dispatch('updateExample', data).then(()=>{
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
                    this.$store.dispatch('createExample', data).then(()=>{
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
            }
        },
        areSubExamplesValid(parentExample){
            let valid = true;
            if(parentExample.subExamples){
                for(const example of parentExample.subExamples){
                    if(!this.isExampleValid(example)){
                        valid = false;
                        this.setSelectedExample(example);
                        this.$nextTick(()=>{
                            this.$refs[`formExample${this.activeTab-1}`][0].reportValidity();
                        });
                        break;
                    }
                }
            }
            return valid;
        },
        isExampleValid(example){
            return example.name && example.description && example.points && example.weighting;
        },
        buildExample(name, order){
            return {
                name: `${name} ${order +1}`,
                weighting: 1,
                points: 0,
                subExamples: [],
                order,
                validator: '',
                supportedFileTypes: []
            }
        },
        newExample(){
            this.sheetInfo.examples.push(this.buildExample(this.$t('example.name'), this.sheetInfo.examples.length));
            this.activeTab = this.sheetInfo.examples.length;
        },
        deleteExample(id, index){
            this.sheetInfo.examples.splice(index, 1);
            this.adjustSubExampleOrder(this.sheetInfo.examples, index);
            //server needs to adjust/update all orders
            if(id){
                this.$store.dispatch('deleteExample', id).then(()=>{
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
                this.$bvToast.toast(this.$t('example.deleted'), {
                    title: this.$t('success'),
                    variant: 'success',
                    appendToast: true
                });
            }
        },
        adjustSubExampleOrder(examples, index){
            if(index < examples.length){
                for(let i = index; i < examples.length; i++){
                    examples[i].order--;
                }
            }
        },
        setSelectedExample(example){
            this.selectedExample = example;
        }
    }
}
</script>